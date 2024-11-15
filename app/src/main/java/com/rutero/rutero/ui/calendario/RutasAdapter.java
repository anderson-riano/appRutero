package com.rutero.rutero.ui.calendario;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.google.inject.Inject;
import com.rutero.rutero.R;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.ruta.Ruta;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.ui.ingresoPdv.MapActivity;

import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class RutasAdapter extends RecyclerView.Adapter<RutasAdapter.RutaViewHolder> {

    private List<Ruta> rutas;
    private static Context context;
    private Usuario usuario;

    @Inject
    private IUsuarioManager usuarioManager;

    public RutasAdapter(Context context, List<Ruta> rutas, Usuario usuario) {
        this.context = context;
        this.rutas = rutas;
        this.usuario = usuario;
    }

    @NonNull
    @Override
    public RutaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_view, parent, false);

        final RoboInjector injector = RoboGuice.getInjector(context);
        injector.injectMembersWithoutViews(this);

        return new RutaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RutaViewHolder holder, int position) {
        Ruta ruta = rutas.get(position);
        holder.bind(ruta);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (ruta.getEstadoRutaId() == 1) {
                    // Abrir la MapActivity cuando se haga clic en un elemento
                    usuario.setRuta(ruta);
                    usuarioManager.actualizar(usuario);

                    Navigation.findNavController(v).navigate(R.id.actionMapFragment);
//                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return rutas.size();
    }

    public static class RutaViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconRuta;
        private TextView textViewNombre;
        private TextView textViewFecha;

        public RutaViewHolder(@NonNull View itemView) {
            super(itemView);
            iconRuta = itemView.findViewById(R.id.iconRuta);
            textViewNombre = itemView.findViewById(R.id.textViewNombre);
            textViewFecha = itemView.findViewById(R.id.textViewFecha);
        }

        public void bind(Ruta ruta) {
            String estado = (ruta.getEstadoRutaId() == 1) ? "Sin Visitar - " : "Visitado - ";

            // Apply tint using the color resource
            int color = ContextCompat.getColor(context, ((ruta.getEstadoRutaId() == 1) ? R.color.app : R.color.teal_700));
            iconRuta.setColorFilter(color, PorterDuff.Mode.SRC_IN);

            textViewNombre.setText(estado + ruta.getNombre());
            textViewFecha.setText(ruta.getFechaVisita() + " " + ruta.getHoraIngreso());
        }
    }
}
