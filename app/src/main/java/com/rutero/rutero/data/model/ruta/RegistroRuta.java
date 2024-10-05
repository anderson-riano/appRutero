package com.rutero.rutero.data.model.ruta;

import java.io.Serializable;

public class RegistroRuta implements Serializable {
    private int ruta_id;

    private String lat;

    private String lon;

    private int ruta_ejecutada_id;

    private Ruta ruta;

    public void setRutaId(int ruta_id) {
        this.ruta_id = ruta_id;
    }

    public int getRutaId() {
        return ruta_id;
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

    public void setRutaEjecutadaId(int ruta_ejecutada_id) {
        this.ruta_ejecutada_id = ruta_ejecutada_id;
    }

    public int getRutaEjecutadaId() {
        return ruta_ejecutada_id;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Ruta getRuta() {
        return ruta;
    }
}

