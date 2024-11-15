package com.rutero.rutero.data.model.ruta;

import java.io.Serializable;

public class Ruta implements Serializable {
    private int ruta_id;
    private int estado_ruta_id;
    private int pdv_id;
    private String nombre;
    private String lat;
    private String lon;
    private String rango;
    private String fecha_visita;
    private String hora_ingreso;
    private String tiempo_visita;

    public void setRutaId(int ruta_id) {
        this.ruta_id = ruta_id;
    }

    public int getRutaId() {
        return ruta_id;
    }

    public void setEstadoRutaId(int estado_ruta_id) {
        this.estado_ruta_id = estado_ruta_id;
    }

    public int getEstadoRutaId() {
        return estado_ruta_id;
    }

    public void setPdvId(int pdv_id) {
        this.pdv_id = pdv_id;
    }

    public int getPdvId() {
        return pdv_id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLat() {
        return lat;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLon() {
        return lon;
    }

    public void setRango(String rango) {
        this.rango = rango;
    }

    public String getRango() {
        return rango;
    }

    public void setFechaVisita(String fecha_visita) {
        this.fecha_visita = fecha_visita;
    }

    public String getFechaVisita() {
        return fecha_visita;
    }

    public void setHoraIngreso(String hora_ingreso) {
        this.hora_ingreso = hora_ingreso;
    }

    public String getHoraIngreso() {
        return hora_ingreso;
    }

    public void setTiempoVisita(String tiempo_visita) {
        this.tiempo_visita = tiempo_visita;
    }

    public String getTiempo_Visita() {
        return tiempo_visita;
    }
}
