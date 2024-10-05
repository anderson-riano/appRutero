package com.rutero.rutero.util;

import com.google.inject.AbstractModule;
import com.rutero.rutero.data.manager.api.IUsuarioManager;
import com.rutero.rutero.data.manager.impl.UsuarioManager;

public class ModuloConfiguracion extends AbstractModule {
    @Override
    protected void configure() {
        bindManagers();
    }

    private void bindManagers() {
        bind(IUsuarioManager.class).to(UsuarioManager.class);
    }
}
