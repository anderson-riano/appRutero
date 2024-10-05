package com.rutero.rutero.util;

import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.data.model.usuario.UsuarioResponse;
import com.rutero.rutero.data.model.ruta.RegistroRuta;
import com.rutero.rutero.data.model.ruta.RutaResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    @GET("endpoint")
    Call<ApiResponse> obtenerDatos();

    @POST("login-app")
    Call<ApiResponse<UsuarioResponse>> loginApp(@Body Usuario usuario);

    @GET("app-get-rutas")
    Call<ApiResponse<RutaResponse>> getRutasForDate(@Query("token") String token, @Query("fecha") String fecha);

    @POST("app-set-rutas")
    Call<ApiResponse> setRegistroRuta(@Query("token") String token, @Body RegistroRuta registroRuta);

    @POST("app-set-out-rutas")
    Call<ApiResponse> setOutRegistroRuta(@Query("token") String token, @Body RegistroRuta registroRuta);
}
