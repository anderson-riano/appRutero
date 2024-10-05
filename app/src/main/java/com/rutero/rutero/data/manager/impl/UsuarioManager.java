package com.rutero.rutero.data.manager.impl;

import com.google.inject.Inject;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.model.usuario.Usuario;
import com.rutero.rutero.util.BaseDeDatosHelper;

import java.sql.SQLException;

public class UsuarioManager extends CrudManager<Usuario, Integer>
        implements IUsuarioManager {

    /**
     * Constructor para el CRUD Manager
     *
     * @param helper
     *         Helper de la BD
     */
    @Inject
    protected UsuarioManager(BaseDeDatosHelper helper) throws SQLException {
        super(helper, Usuario.class);
    }
}
