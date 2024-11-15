package com.rutero.rutero.data.model.producto;

public class Producto {

    private int productoId;
    private int pdvId;
    private String nombreCategoria;
    private String nombreSubcategoria;
    private String nombre;
    private int stock;

    public void setProductoId(int productoId) {
        this.productoId = productoId;
    }

    public int getProductoId() {
        return productoId;
    }

    public void setPdvId(int pdvId) {
        this.pdvId = pdvId;
    }

    public int getPdvId() {
        return pdvId;
    }

    public void setNombreCategoria(String nombreCategoria) {
        this.nombreCategoria = nombreCategoria;
    }

    public String getNombreCategoria() {
        return nombreCategoria;
    }

    public void setNombreSubcategoria(String nombreSubcategoria) {
        this.nombreSubcategoria = nombreSubcategoria;
    }

    public String getNombreSubcategoria() {
        return nombreSubcategoria;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public int getStock() {
        return stock;
    }
}
