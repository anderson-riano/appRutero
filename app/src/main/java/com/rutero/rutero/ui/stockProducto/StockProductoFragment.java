package com.rutero.rutero.ui.stockProducto;

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
import com.rutero.rutero.data.model.producto.Producto;
import com.rutero.rutero.data.model.producto.ProductoResponse;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.databinding.FragmentStockProductoBinding;
import com.rutero.rutero.util.ApiResponse;
import com.rutero.rutero.util.ApiService;
import com.rutero.rutero.util.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class StockProductoFragment extends Fragment {

    private FragmentStockProductoBinding binding;

    @Inject
    private IUsuarioManager usuarioManager;

    private ApiService apiService;

    private Usuario usuario;

    private RecyclerView recyclerView;

    private ProductosAdapter adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        binding = FragmentStockProductoBinding.inflate(inflater, container, false);
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

        // Obtener productos activos
        obtenerProductos();
    }

    private void obtenerProductos() {

        Call<ApiResponse<ProductoResponse>> call = apiService.getProductos(usuario.getToken(), usuario.getRuta().getPdvId());
        call.enqueue(new Callback<ApiResponse<ProductoResponse>>() {

            @Override
            public void onResponse(Call<ApiResponse<ProductoResponse>> call, Response<ApiResponse<ProductoResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ApiResponse<ProductoResponse> apiResponse = response.body();
                    ProductoResponse rutaResponse = apiResponse.getBody();
                    if (rutaResponse != null) {
                        List<Producto> productos = rutaResponse.getProductos();
                        mostrarProductosEnRecyclerView(productos);
                    } else {
                        List<Producto> productos = new ArrayList<Producto>();
                        mostrarProductosEnRecyclerView(productos);
                    }
                } else {
                    Toast.makeText(getContext(), "Error al obtener Productos", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<ProductoResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "Error al obtener Productos: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarProductosEnRecyclerView(List<Producto> productos) {
        adapter = new ProductosAdapter(getContext(), productos, usuario);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
