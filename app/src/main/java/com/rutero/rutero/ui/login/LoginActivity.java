package com.rutero.rutero.ui.login;

import android.app.Activity;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.inject.Inject;
import com.rutero.rutero.R;
import com.rutero.rutero.RoboAppCompatActivity;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.data.model.usuario.UsuarioResponse;
import com.rutero.rutero.ui.base.BaseMenuActivity;
import com.rutero.rutero.util.ApiResponse;
import com.rutero.rutero.util.ApiService;
import com.rutero.rutero.util.RetrofitClient;
import com.rutero.rutero.databinding.ActivityLoginBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import roboguice.RoboGuice;
import roboguice.inject.RoboInjector;

public class LoginActivity extends RoboAppCompatActivity {

    private LoginViewModel loginViewModel;
    private ActivityLoginBinding binding;

    ProgressBar loadingProgressBarView;

    private ApiService apiService;

    @Inject
    private IUsuarioManager usuarioManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final RoboInjector injector = RoboGuice.getInjector(this);
        injector.injectMembersWithoutViews(this);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        loginViewModel = new ViewModelProvider(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        final EditText usernameEditText = binding.username;
        final EditText passwordEditText = binding.password;
        final Button loginButton = binding.login;
        final ProgressBar loadingProgressBar = binding.loading;
        loadingProgressBarView = loadingProgressBar;
        // Inicializa ApiService
        apiService = RetrofitClient.getRetrofitInstance().create(ApiService.class);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy login activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                enviarDatos(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });
    }

    private void enviarDatos(String login, String clave) {
        Usuario usuario = new Usuario();
        usuario.setUsuario(login);
        usuario.setClave(clave);
        Call<ApiResponse<UsuarioResponse>> call = apiService.loginApp(usuario);
        call.enqueue(new Callback<ApiResponse<UsuarioResponse>>() {
            @Override
            public void onResponse(Call<ApiResponse<UsuarioResponse>> call, Response<ApiResponse<UsuarioResponse>> response) {
                loadingProgressBarView.setVisibility(View.GONE);
                if (response.isSuccessful()) {
                    ApiResponse<UsuarioResponse> apiResponse = response.body();
                    if (apiResponse.getStatus() == 1) {
                        UsuarioResponse usuarioResponse = apiResponse.getBody();
                        Usuario usuario = usuarioResponse.getUsuario();
                        usuarioManager.crearOActualizar(usuario);

                        String msg = usuario.getNombre();
                        String welcome = getString(R.string.welcome) + msg;
                        // TODO : initiate successful logged in experience
                        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();

                        finish();
                        Intent intent = new Intent(LoginActivity.this, BaseMenuActivity.class);
                        startActivity(intent);
                    } else {
                        Log.e("API_ERROR", "Error: " + apiResponse.getMessage());
                        Toast.makeText(getApplicationContext(), apiResponse.getMessage(), Toast.LENGTH_LONG).show();
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.code() + " " + response.message());
                }
                // Procesar el mensaje
            }

            @Override
            public void onFailure(Call<ApiResponse<UsuarioResponse>> call, Throwable t) {
                loadingProgressBarView.setVisibility(View.GONE);
                Log.e("API_ERROR", "Error: " + t.getMessage());
                Toast.makeText(getApplicationContext(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}