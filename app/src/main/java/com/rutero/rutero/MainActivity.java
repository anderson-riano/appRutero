package com.rutero.rutero;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.google.inject.Inject;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.ui.base.BaseMenuActivity;
import com.rutero.rutero.ui.login.LoginActivity;

import java.util.List;

import roboguice.RoboGuice;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import roboguice.inject.RoboInjector;

@ContentView(R.layout.activity_main)
public class MainActivity extends RoboAppCompatActivity {

    /** Tag para logs **/
    private static final String TAG_LOG = MainActivity.class.getName();

    /** Id de los permisos iniciales **/
    private static final int INITIAL_REQUEST = 1337;

    /** Permisos iniciales para > Marshmallow **/
    private static final String[] INITIAL_PERMS = {};

    /** Permisos iniciales para > Android 11 **/
    private static final String[] INITIAL_PERMS29 = {};

    /** Permisos iniciales para > Android 14 **/
    private static final String[] INITIAL_PERMS34 = {};

    @Inject
    private IUsuarioManager usuarioManager;

    /** Logo de DBD+ **/
    @InjectView(R.id.logo_visualiti)
    private ImageView mLogoDbdPlusIv;

    /** Marca Registrada **/
    @InjectView(R.id.marca_registrada_tv)
    private TextView mMarcaRegistradaTv;

    /** Background RelativeLayout **/
    @InjectView(R.id.background_rl)
    private View mBackgroundRl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        RoboGuice.setUseAnnotationDatabases(false);
        RoboInjector injector = RoboGuice.getInjector(this);
        injector.injectMembers(this);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        if (validarPermisos()) {
            init();
        } else {
            permisosUbicacionAlert();
        }
    }

    /**
     * Este métod ovalida los permisos según sistema operativo
     *
     * @return True si están validados todos los persmisos. False si existe al menos un permiso
     * garantizado
     */
    private boolean validarPermisos() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        String[] INITIAL_PERMS_VALIDATE = INITIAL_PERMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            INITIAL_PERMS_VALIDATE = INITIAL_PERMS29;
        }
        for (String permission : INITIAL_PERMS_VALIDATE) {
            if (!hasPermission(permission)) {
                return false;
            }
        }
        return true;
    }

    static {
        RoboGuice.setUseAnnotationDatabases(false);
    }

    /**
     * Indica si tiene permisos con el parámetro dado
     *
     * @param perm
     *         Permiso a validar
     *
     * @return True si tiene permsiso. False de lo contrario
     */
    @TargetApi(Build.VERSION_CODES.M)
    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == checkSelfPermission(perm));
    }

    private void permisosUbicacionAlert() {
        String[] INITIAL_PERMS_VALIDATE = INITIAL_PERMS;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            INITIAL_PERMS_VALIDATE = INITIAL_PERMS29;
        } else if (Build.VERSION.SDK_INT >= 34) {
            INITIAL_PERMS_VALIDATE = INITIAL_PERMS34;
        }
        requestPermissions(INITIAL_PERMS_VALIDATE, INITIAL_REQUEST);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == INITIAL_REQUEST) {
            if (!validarPermisos()) {
                Toast.makeText(MainActivity.this, "La aplicación puede no funcionar de manera correcta sin estos permisos.", Toast.LENGTH_LONG).show();
            }
            init();
        }
    }

    /**
     * Este método inicializa toda la aplicación
     */
    private void init() {

        animacionLogo(mLogoDbdPlusIv);
        animacionMarcaRegistrada(mMarcaRegistradaTv);

        int myTimer = 2500;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Usuario> usuarioList = usuarioManager.todos();

                if (usuarioList != null && usuarioList.size() > 0) {
                    Intent i = new Intent(MainActivity.this, BaseMenuActivity.class);
                    Pair<View, String>[] transitionPairs = new Pair[2];
                    transitionPairs[0] = Pair.create((View) mLogoDbdPlusIv, getString(R.string.trans_logo));
                    transitionPairs[1] =
                            Pair.create((View) mMarcaRegistradaTv, getString(R.string.trans_marca_regi));
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, transitionPairs);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i, options.toBundle());
                } else{
                    Intent i = new Intent(MainActivity.this, LoginActivity.class);
                    Pair<View, String>[] transitionPairs = new Pair[2];
                    transitionPairs[0] = Pair.create((View) mLogoDbdPlusIv, getString(R.string.trans_logo));
                    transitionPairs[1] =
                            Pair.create((View) mMarcaRegistradaTv, getString(R.string.trans_marca_regi));
                    ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(MainActivity.this, transitionPairs);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i, options.toBundle());
                }
            }
        }, myTimer);
    }

    /**
     * Este método anima una vista diaria para su entrada en fadeIn y escalar
     *
     * @param view
     *         Vista a animar
     */
    private void animacionLogo(View view) {
        int duration = 1000;
        ObjectAnimator scaleXAnimation = ObjectAnimator.ofFloat(view, "scaleX", 5.0F, 1.0F);
        scaleXAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleXAnimation.setDuration(duration);
        ObjectAnimator scaleYAnimation = ObjectAnimator.ofFloat(view, "scaleY", 5.0F, 1.0F);
        scaleYAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleYAnimation.setDuration(duration);
        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(view, "alpha", 0.0F, 1.0F);
        alphaAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        alphaAnimation.setDuration(duration);
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.play(scaleXAnimation).with(scaleYAnimation).with(alphaAnimation);
        animatorSet.start();
    }

    /**
     * Animación para la marca registrada
     *
     * @param view
     *         Vista a animar
     */
    private void animacionMarcaRegistrada(View view) {
        ObjectAnimator alphaAnimation = ObjectAnimator.ofFloat(view, "alpha", 0.0F, 1.0F);
        alphaAnimation.setStartDelay(1000);
        alphaAnimation.setDuration(500);
        alphaAnimation.start();
    }
}
