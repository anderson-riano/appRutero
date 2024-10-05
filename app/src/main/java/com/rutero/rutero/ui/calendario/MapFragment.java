package com.rutero.rutero.ui.calendario;

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
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.inject.Inject;
import com.rutero.rutero.R;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.ruta.RegistroRuta;
import com.rutero.rutero.data.model.ruta.Ruta;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.databinding.FragmentMapBinding;
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

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private FragmentMapBinding binding;

    @Inject
    private IUsuarioManager usuarioManager;

    private ApiService apiService;

    private Usuario usuario;

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private MapView mapView;
    private GoogleMap googleMap;
    private double latitud;
    private double longitud;
    private Ruta ruta;
    private String token;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location ubicacionActual;

    private TextView textViewPDV;
    private TextView textViewFecha;
    private TextView textViewHora;
    private MaterialButton btnEjecutarApi;

    private RegistroRuta registroRuta;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RoboInjector injector = RoboGuice.getInjector(getContext());
        injector.injectMembersWithoutViews(this);

        usuario = usuarioManager.todos().get(0);
        ruta = usuario.getRuta();

        token = usuario.getToken();
        // Obtener coordenadas de latitud y longitud pasadas desde la actividad anterior
        latitud = Double.parseDouble(ruta.getLat());
        longitud = Double.parseDouble(ruta.getLon());

        // Inicializar el MapView
        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Inicializar el FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        // Verificar permisos y obtener la ubicación del dispositivo

        textViewPDV = binding.textViewPDV;
        textViewPDV.setText("PDV: " + ruta.getNombre());

        textViewFecha = binding.textViewFecha;
        textViewFecha.setText("Fecha: " + ruta.getFechaVisita());

        textViewHora = binding.textViewHora;
        textViewHora.setText("Hora: " + ruta.getHoraIngreso());

        btnEjecutarApi = binding.btnEjecutarApi;
        btnEjecutarApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerUbicacionYCalcularDistancia();
            }
        });
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

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                verificarConfiguracionUbicacion();
            } else {
                Toast.makeText(getContext(), "Permiso denegado para acceder a la ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    private void obtenerUbicacionDispositivo() {
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        ubicacionActual = location;
                        if (googleMap != null) {
                            googleMap.setMyLocationEnabled(true);

                            // Añadir marcador en la ubicación especificada
                            LatLng ubicacion = new LatLng(latitud, longitud);
                            // googleMap.addMarker(new MarkerOptions().position(ubicacion).title("Ubicación"));
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(ubicacion, 15));
                            MarkerOptions markerOptions = new MarkerOptions().position(ubicacion).title(ruta.getNombre());
                            Marker pdvMarker = googleMap.addMarker(markerOptions);
                            double distancia = calcularDistancia(ubicacionActual.getLatitude(), ubicacionActual.getLongitude(), latitud, longitud);
                            pdvMarker.setSnippet(String.format("Distancia de: ", distancia / 1000));

                            googleMap.addCircle(new CircleOptions()
                                    .center(ubicacion)
                                    .radius(Double.parseDouble(ruta.getRango()))
                                    .strokeWidth(5)
                                    .fillColor(0x80309030));
                        }
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


    private void obtenerUbicacionYCalcularDistancia() {
        if (ubicacionActual != null) {
            double distancia = calcularDistancia(ubicacionActual.getLatitude(), ubicacionActual.getLongitude(), latitud, longitud);
            if (distancia > Integer.parseInt(ruta.getRango())) {
                Toast.makeText(getContext(), "Fuera del Rango permitido", Toast.LENGTH_LONG).show();
            } else if (!validarGps()) {
                Toast.makeText(getContext(), "Utilizando aplicacion de FAKE GPS. INGRESO INVALIDO", Toast.LENGTH_LONG).show();
            } else {
                ejecutarApiConUbicacion(ubicacionActual.getLatitude(), ubicacionActual.getLongitude());
            }
            // Aquí puedes realizar la llamada API enviando latitud y longitud del usuario
        } else {
            Toast.makeText(getContext(), "Ubicación actual no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private double calcularDistancia(double lat1, double lon1, double lat2, double lon2) {
        float[] resultados = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, resultados);
        return resultados[0];
    }

    private void ejecutarApiConUbicacion(double latUsuario, double lonUsuario) {
        ApiService apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);
        // Aquí puedes ajustar el endpoint y los parámetros según tu API
        registroRuta = new RegistroRuta();
        registroRuta.setRutaId(ruta.getRutaId());
        registroRuta.setLat(Double.toString(latUsuario));
        registroRuta.setLon(Double.toString(lonUsuario));

        Call<ApiResponse> call = apiService.setRegistroRuta(token, registroRuta);
        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse apiResponse = response.body();
                    if (apiResponse.getStatus() == 1) {
                        registroRuta.setRutaEjecutadaId(Integer.parseInt(apiResponse.getMessage()));
                        Toast.makeText(getContext(), "Ingreso Exitoso", Toast.LENGTH_LONG).show();
                        usuario.setPdvActual(registroRuta);
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
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        getLocationPermission();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        mapView.onDestroy();
    }
}
