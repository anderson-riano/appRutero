package com.rutero.rutero.ui.stockProducto;

import android.content.Context;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;
import com.rutero.rutero.R;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.producto.Producto;
import com.rutero.rutero.data.model.ruta.Ruta;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.ui.calendario.RutasAdapter;
import com.rutero.rutero.util.ApiResponse;
import com.rutero.rutero.util.ApiService;
import com.rutero.rutero.util.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class ProductosAdapter extends RecyclerView.Adapter<ProductosAdapter.ProductoViewHolder> {

    private List<Producto> productos;
    private static Context context;
    private Usuario usuario;

    private ApiService apiService;

    public ProductosAdapter(Context context, List<Producto> productos, Usuario usuario) {
        this.context = context;
        this.productos = productos;
        this.usuario = usuario;
    }

    @NonNull
    @Override
    public ProductoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);

        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);

        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        return new ProductosAdapter.ProductoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductoViewHolder holder, int position) {
        Producto producto = productos.get(position);
        holder.bind(producto);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Crear un diálogo de alerta
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("Ingrese la cantidad para " + producto.getNombre());

                // Configurar el EditText para el ingreso de cantidad
                final EditText inputCantidad = new EditText(context);
                inputCantidad.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
                builder.setView(inputCantidad);

                // Configurar botones del diálogo
                builder.setPositiveButton("Aceptar", (dialog, which) -> {
                    String cantidadStr = inputCantidad.getText().toString();
                    if (!cantidadStr.isEmpty()) {
                        int cantidad = Integer.parseInt(cantidadStr);
                        // Actualizar la cantidad en el modelo, si es necesario
                        producto.setStock(cantidad);
                        // Llamada al servicio setRegistroRuta
                        Call<ApiResponse> call = apiService.setRegistroProducto(usuario.getToken(), producto);
                        call.enqueue(new Callback<ApiResponse>() {
                            @Override
                            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    ApiResponse apiResponse = response.body();
                                    if (apiResponse.getStatus() == 1) {
                                        // Aquí puedes manejar la cantidad ingresada
                                        Toast.makeText(context, "Cantidad ingresada: " + cantidad, Toast.LENGTH_SHORT).show();
                                        notifyItemChanged(position);
                                    } else {
                                        Log.e("API_ERROR", "Error: " + apiResponse.getMessage());
                                        Toast.makeText(context, apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                                    }
                                } else {
                                    Log.e("API_ERROR", "Error: " + response.code() + " " + response.message());
                                }
                            }

                            @Override
                            public void onFailure(Call<ApiResponse> call, Throwable t) {
                                Log.e("API_ERROR", "Error: " + t.getMessage());
                                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        });
                    } else {
                        Toast.makeText(context, "Debe ingresar una cantidad", Toast.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

                // Mostrar el diálogo
                builder.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return productos.size();
    }

    public static class ProductoViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconRuta;
        private TextView textViewNombre;
        private TextView textViewFecha;

        public ProductoViewHolder(@NonNull View itemView) {
            super(itemView);
            iconRuta = itemView.findViewById(R.id.iconRuta);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewFecha = itemView.findViewById(R.id.textViewFecha);
        }

        public void bind(Producto producto) {

            // Apply tint using the color resource
            iconRuta.setImageResource(R.drawable.ic_stock);

            textViewNombre.setText(producto.getNombre());
            textViewFecha.setText("Stock: " + producto.getStock());
        }
    }
}
