package com.rutero.rutero.ui.calendario;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.ruta.Ruta;
import com.rutero.rutero.data.model.ruta.RutaResponse;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.databinding.FragmentCalendarioBinding;
import com.rutero.rutero.util.ApiResponse;
import com.rutero.rutero.util.ApiService;
import com.rutero.rutero.util.RetrofitClient;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class CalendarioFragment extends Fragment {

    private FragmentCalendarioBinding binding;

    @Inject
    private IUsuarioManager usuarioManager;

    private ApiService apiService;

    private Usuario usuario;

    private RecyclerView recyclerView;

    private Button btnSeleccionarFecha;

    private Calendar calendario;

    private RutasAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentCalendarioBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final RoboInjector injector = RoboGuice.getInjector(getContext());
        injector.injectMembersWithoutViews(this);

        usuario = usuarioManager.todos().get(0);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        btnSeleccionarFecha = binding.seleccionarFechaBtn;

        calendario = Calendar.getInstance();

        // Obtener rutas para la fecha actual
        obtenerRutasParaFecha(obtenerFechaActual());

        btnSeleccionarFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mostrarDatePicker();
            }
        });
    }

    private String obtenerFechaActual() {
        Calendar calendarioActual = Calendar.getInstance();
        int año = calendarioActual.get(Calendar.YEAR);
        int mes = calendarioActual.get(Calendar.MONTH) + 1;
        int día = calendarioActual.get(Calendar.DAY_OF_MONTH);
        return año + "-" + mes + "-" + día;
    }

    private void obtenerRutasParaFecha(String fechaSeleccionada) {
        // Aquí obtendrías la fecha actual en el formato adecuado, por ejemplo:
        // String fechaActual = obtenerFechaActualEnFormato("yyyy-MM-dd");

        // En este ejemplo, supongamos que la fecha actual es "2024-05-27"
        String fechaActual = fechaSeleccionada;

        Call<ApiResponse<RutaResponse>> call = apiService.getRutasForDate(usuario.getToken(), fechaActual);
        call.enqueue(new Callback<ApiResponse<RutaResponse>>() {

            @Override
            public void onResponse(Call<ApiResponse<RutaResponse>> call, Response<ApiResponse<RutaResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<RutaResponse> apiResponse = response.body();
                    RutaResponse rutaResponse = apiResponse.getBody();
                    if (rutaResponse != null) {
                        List<Ruta> rutas = rutaResponse.getRutas();
                        mostrarRutasEnRecyclerView(rutas);
                    } else {
                        List<Ruta> rutas = new ArrayList<Ruta>();
                        mostrarRutasEnRecyclerView(rutas);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener rutas", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<RutaResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al obtener rutas: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarRutasEnRecyclerView(List<Ruta> rutas) {
        adapter = new RutasAdapter(getContext(), rutas, usuario);
        recyclerView.setAdapter(adapter);
    }

    public void mostrarDatePicker() {
        int año = calendario.get(Calendar.YEAR);
        int mes = calendario.get(Calendar.MONTH);
        int día = calendario.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // Aquí obtienes la fecha seleccionada
                        String fechaSeleccionada = year + "-" + (month + 1) + "-" + dayOfMonth;
                        obtenerRutasParaFecha(fechaSeleccionada);
                    }
                }, año, mes, día);
        datePickerDialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}