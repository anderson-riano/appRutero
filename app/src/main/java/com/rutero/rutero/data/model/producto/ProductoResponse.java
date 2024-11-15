package com.rutero.rutero.data.model.producto;

import java.util.List;

public class ProductoResponse {

    private List<Producto> productos;

    public void setProductos(List<Producto> productos) {
        this.productos = productos;
    }

    public List<Producto> getProductos() {
        return productos;
        }

}
