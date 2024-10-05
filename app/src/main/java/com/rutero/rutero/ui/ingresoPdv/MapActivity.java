package com.rutero.rutero.ui.ingresoPdv;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.rutero.rutero.R;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.ui.base.BaseMenuActivity;
import com.rutero.rutero.util.ApiResponse;
import com.rutero.rutero.util.ApiService;
import com.rutero.rutero.util.RetrofitClient;
import com.rutero.rutero.data.model.ruta.RegistroRuta;
import com.rutero.rutero.data.model.ruta.Ruta;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private MapView mapView;
    private GoogleMap googleMap;
    private Usuario usuario;
    private double latitud;
    private double longitud;
    private Ruta ruta;
    private String token;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location ubicacionActual;

    private TextView textViewPDV;
    private TextView textViewFecha;
    private TextView textViewHora;
    private Button btnEjecutarApi;
    private Button btnSalirApi;

    private RegistroRuta registroRuta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        ruta = (Ruta) getIntent().getSerializableExtra("RUTA");
        usuario = (Usuario) getIntent().getSerializableExtra("USUARIO");
        token = usuario.getToken();
        // Obtener coordenadas de latitud y longitud pasadas desde la actividad anterior
        latitud = Double.parseDouble(ruta.getLat());
        longitud = Double.parseDouble(ruta.getLon());

        // Inicializar el MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Inicializar el FusedLocationProviderClient
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Verificar permisos y obtener la ubicación del dispositivo

        textViewPDV = findViewById(R.id.textViewPDV);
        textViewPDV.setText("PDV: " + ruta.getNombre());
        textViewFecha = findViewById(R.id.textViewFecha);
        textViewFecha.setText("Fecha: " + ruta.getFechaVisita());
        textViewHora = findViewById(R.id.textViewHora);
        textViewHora.setText("Hora: " + ruta.getHoraIngreso());
        btnEjecutarApi = findViewById(R.id.btnEjecutarApi);
        btnSalirApi = findViewById(R.id.btnSalirApi);

        btnEjecutarApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerUbicacionYCalcularDistancia();
            }
        });

        btnSalirApi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                obtenerUbicacionYSalirPdv();
            }
        });
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            verificarConfiguracionUbicacion();
        } else {
            ActivityCompat.requestPermissions(this,
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

        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, locationSettingsResponse -> obtenerUbicacionDispositivo());

        task.addOnFailureListener(this, e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapActivity.this, 100);
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
                Toast.makeText(this, "Permiso denegado para acceder a la ubicación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                obtenerUbicacionDispositivo();
            } else {
                Toast.makeText(this, "Debe habilitar la ubicación para usar esta función", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void obtenerUbicacionDispositivo() {
        try {
            Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
            locationResult.addOnSuccessListener(this, new OnSuccessListener<Location>() {
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
                Toast.makeText(this, "Fuera del Rango permitido", Toast.LENGTH_LONG).show();
            } else if (!validarGps()) {
                Toast.makeText(this, "Utilizando aplicacion de FAKE GPS. INGRESO INVALIDO", Toast.LENGTH_LONG).show();
            } else {
                ejecutarApiConUbicacion(ubicacionActual.getLatitude(), ubicacionActual.getLongitude());
            }
            // Aquí puedes realizar la llamada API enviando latitud y longitud del usuario
        } else {
            Toast.makeText(this, "Ubicación actual no disponible", Toast.LENGTH_SHORT).show();
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
                        Toast.makeText(getApplicationContext(), "Ingreso Exitoso", Toast.LENGTH_LONG).show();
                        mapView.setVisibility(View.GONE);
                        btnEjecutarApi.setVisibility(View.GONE);
                        btnSalirApi.setVisibility(View.VISIBLE);
                    } else {
                        Log.e("API_ERROR", "Error: " + apiResponse.getMessage());
                        Toast.makeText(getApplicationContext(), apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void obtenerUbicacionYSalirPdv() {
        if (ubicacionActual != null) {
            salirApiConUbicacion(ubicacionActual.getLatitude(), ubicacionActual.getLongitude());
            // Aquí puedes realizar la llamada API enviando latitud y longitud del usuario
        } else {
            Toast.makeText(this, "Ubicación actual no disponible", Toast.LENGTH_SHORT).show();
        }
    }

    private void salirApiConUbicacion(double latUsuario, double lonUsuario) {
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
                        // Abrir la MapActivity cuando se haga clic en un elemento
                        Intent intent = new Intent(MapActivity.this, BaseMenuActivity.class);
                        intent.putExtra("USUARIO", usuario);
                        startActivity(intent);
                    } else {
                        Log.e("API_ERROR", "Error: " + apiResponse.getMessage());
                        Toast.makeText(getApplicationContext(), apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.code() + " " + response.message());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        getLocationPermission();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }
}
