package com.rutero.rutero.ui.base;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.inject.Inject;
import com.rutero.rutero.MainActivity;
import com.rutero.rutero.R;
import com.rutero.rutero.RoboAppCompatActivity;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.databinding.ActivityBaseBinding;

import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class BaseMenuActivity extends RoboAppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ActivityBaseBinding binding;
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 2;

    private int selectedNavItem = R.id.nav_home;

    @Inject
    private IUsuarioManager usuarioManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoboInjector injector = RoboGuice.getInjector(this);
        injector.injectMembersWithoutViews(this);

        binding = ActivityBaseBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.appBarMain.toolbar);
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        Usuario usuario = usuarioManager.todos().get(0);
        if (usuario.getPdvActual() != null) {
            Menu navMenu = navigationView.getMenu();
            MenuItem rutaItem = navMenu.findItem(R.id.nav_calendario);
            rutaItem.setVisible(false); // This hides the item
        }

        // Hide the nav_cerrar_sesion item

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                selectedNavItem = id;

                if (id == R.id.nav_home) {
                    navController.navigate(R.id.nav_home);
                } else if (id == R.id.nav_calendario) {
                    navController.navigate(R.id.nav_calendario);
                } else if (id == R.id.nav_cerrar_sesion) {
                    usuarioManager.eliminarTodo();
                    Intent i = new Intent(BaseMenuActivity.this, MainActivity.class);
                    startActivity(i);
                    return true;
                }

                DrawerLayout drawer = binding.drawerLayout;
                drawer.closeDrawer(GravityCompat.START);
                return true;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    private void handleFabClick() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        switch (selectedNavItem) {
            default:
                break;
        }
    }
}