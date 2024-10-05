package com.rutero.rutero.ui.home;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.inject.Inject;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.ruta.RegistroRuta;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.databinding.FragmentHomeBinding;
import com.rutero.rutero.ui.base.BaseMenuActivity;
import com.rutero.rutero.ui.ingresoPdv.MapActivity;
import com.rutero.rutero.util.ApiResponse;
import com.rutero.rutero.util.ApiService;
import com.rutero.rutero.util.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class HomeFragment extends Fragment {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Inject
    private IUsuarioManager usuarioManager;

    private FragmentHomeBinding binding;

    private Usuario usuario;

    private TextView pdvActual;

    private FloatingActionButton floatingActionButton;

    private Location ubicacionActual;

    private FusedLocationProviderClient fusedLocationProviderClient;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RoboInjector injector = RoboGuice.getInjector(getContext());
        injector.injectMembersWithoutViews(this);

        usuario = usuarioManager.todos().get(0);

        pdvActual = binding.pdvActual;
        floatingActionButton = binding.floatingActionButton;

        if (usuario.getPdvActual() != null) {
            pdvActual.setText(usuario.getRuta().getNombre());
            floatingActionButton.setVisibility(View.VISIBLE);
        } else {
            pdvActual.setText("No hay PDV actualmente");
            floatingActionButton.setVisibility(View.GONE);
        }

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                obtenerUbicacionYSalirPdv();
            }
        });

        // Inicializar el FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        getLocationPermission();

    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            verificarConfiguracionUbicacion();
        } else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void verificarConfiguracionUbicacion() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(5000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient client = LocationServices.getSettingsClient(getContext());
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(getActivity(), locationSettingsResponse -> obtenerUbicacionDispositivo());

        task.addOnFailureListener(getActivity(), e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(getActivity(), 100);
                } catch (IntentSender.SendIntentException sendEx) {
                    // Ignorar el error
                }
            }
        });
    }

    private void obtenerUbicacionDispositivo() {
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        ubicacionActual = location;
                    }
                }
            });
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //Valida si se usa fake gps
    private boolean validarGps() {
        if (ubicacionActual != null && ubicacionActual.isFromMockProvider()) {
            // Si la ubicación es de un mock provider, no es válida
            return false;
        }
        // Si no es de un mock provider, la ubicación es válida
        return true;
    }

    private void obtenerUbicacionYSalirPdv() {
        if (ubicacionActual != null) {
            if (!validarGps()) {
                Toast.makeText(getContext(), "Utilizando aplicacion de FAKE GPS. SALIDA INVALIDO", Toast.LENGTH_LONG).show();
            } else {
                salirApiConUbicacion(ubicacionActual.getLatitude(), ubicacionActual.getLongitude());
            }
        } else {
            Toast.makeText(getContext(), "Ubicación actual no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void salirApiConUbicacion(double latUsuario, double lonUsuario) {
        String token = usuario.getToken();
        RegistroRuta registroRuta = usuario.getPdvActual();

        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        // Aquí puedes ajustar el endpoint y los parámetros según tu API
        registroRuta.setLat(Double.toString(latUsuario));
        registroRuta.setLon(Double.toString(lonUsuario));

        Call<ApiResponse> call = apiService.setOutRegistroRuta(token, registroRuta);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getStatus() == 1) {
                        Toast.makeText(getContext(), "Salida de Punto Exitosa", Toast.LENGTH_LONG).show();

                        usuario.setPdvActual(null);
                        usuario.setRuta(null);
                        usuarioManager.actualizar(usuario);

                        Activity activity = getActivity();
                        activity.finish();
                        Intent intent = new Intent(getContext(), BaseMenuActivity.class);
                        startActivity(intent);
                    } else {
                        Log.e("API_ERROR", "Error: " + apiResponse.getMessage());
                        Toast.makeText(getContext(), apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(getContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}