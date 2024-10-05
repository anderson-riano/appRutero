package com.rutero.rutero.data.model.usuario;

import com.j256.ormlite.field.DataType;
import com.j256.ormlite.field.DatabaseField;
import com.rutero.rutero.data.model.ruta.RegistroRuta;
import com.rutero.rutero.data.model.ruta.Ruta;

import java.io.Serializable;

public class Usuario implements Serializable {

    @DatabaseField
    private String cliente;

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String usuario;

    private String clave;

    @DatabaseField
    private String token;

    @DatabaseField
    private String nombre;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private Ruta ruta;

    @DatabaseField(dataType = DataType.SERIALIZABLE)
    private RegistroRuta pdvActual;

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getCliente() {
        return cliente;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getClave() {
        return clave;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setRuta(Ruta ruta) {
        this.ruta = ruta;
    }

    public Ruta getRuta() {
        return ruta;
    }

    public void setPdvActual(RegistroRuta pdvActual) {
        this.pdvActual = pdvActual;
    }

    public RegistroRuta getPdvActual() {
        return pdvActual;
    }
}
