package com.rutero.rutero.data.manager.impl;

import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.rutero.rutero.data.manager.api.ICrudManager;
import com.rutero.rutero.util.BaseDeDatosHelper;

import java.sql.SQLException;
import java.util.List;

public class CrudManager<T, Id> implements ICrudManager<T, Id> {

    /** Tag para Logs **/
    private static final String TAG_LOG = CrudManager.class.getName();

    /** Helper de la BD **/
    private BaseDeDatosHelper helper;

    /**
     * DAO de la entidad
     */
    private final Dao<T, Id> dao;

    /** Clase de la entidad **/
    private final Class<T> clazz;

    /**
     * Constructor para el CRUD Manager
     *
     * @param helper
     *         Helper de la BD
     * @param clazz
     *         La clase de la entidad a ser utilizada
     */
    protected CrudManager(BaseDeDatosHelper helper, Class<T> clazz) throws SQLException {
        this.helper = helper;
        dao = helper.getDao(clazz);
        this.clazz = clazz;
    }

    /**
     * Creación (o actualización) de la entidad T. En caso de que la entidad utilice Id
     * autogenerado, éste será cargado en la instancia
     *
     * @param entidad
     *         Instancia de la entidad T a ser creada en la BD
     *
     * @return True si la entidad pudo ser creada, False de lo contrario
     */
    @Override
    public boolean crearOActualizar(T entidad) {
        boolean creado = false;

        try {
            dao.createOrUpdate(entidad);
            creado = true;
        } catch (SQLException e) {
            Log.e(TAG_LOG,
                    String.format("Un error ocurrió mientras se creaba la entidad {%s}", clazz), e);
        }

        return creado;
    }

    /**
     * Busca una entidad por su Id
     *
     * @param id
     *         Id de la entidad a buscar
     *
     * @return Retorna la instancia si existe una coincidencia. De lo contrario, retorna null
     */
    @Override
    public T buscarPorId(Id id) {
        T entidad = null;

        try {
            entidad = dao.queryForId(id);
        } catch (SQLException e) {
            Log.e(TAG_LOG, String.format(
                            "Un error ocurrió mientras se buscaba la entidad {%s} con id {%s}", clazz, id),
                    e);
        }

        return entidad;
    }

    /**
     * Retorna una lista de todas las entidades T almacenadas
     *
     * @return Lista de las entidades almacenadas
     */
    @Override
    public List<T> todos() {
        List<T> todos = null;
        try {

            todos = dao.queryForAll();
        } catch (SQLException e) {
            Log.e(TAG_LOG,
                    String.format("Un error ocurrió recuperando todas las entidades {%s}", clazz),
                    e);
        }
        return todos;
    }

    /**
     * Retorna una lista de todas las entidades T almacenadas POR ESTADO ACTIVO
     *
     * @return Lista de las entidades almacenadas
     */
    @Override
    public List<T> todosEstado(Integer estado) {
        List<T> todos = null;
        try {
            Where<T, Id> where = dao.queryBuilder().where();
            where.eq("estado",estado);
            todos = where.query();
        } catch (SQLException e) {
            Log.e(TAG_LOG,
                    String.format("Un error ocurrió recuperando todas las entidades {%s}", clazz),
                    e);
        }
        return todos;
    }

    /**
     * Retorna una lista de todas las entidades T almacenadas POR ESTADO ACTIVO
     *
     * @return Lista de las entidades almacenadas
     */
    @Override
    public List<T> sinSincronizar() {
        List<T> todos = null;
        try {
            Where<T, Id> where = dao.queryBuilder().where();
            where.eq("sincronizado",false);
            todos = where.query();
        } catch (SQLException e) {
            Log.e(TAG_LOG,
                    String.format("Un error ocurrió recuperando todas las entidades {%s}", clazz),
                    e);
        }
        return todos;
    }

    /**
     * Retorna una lista de todas las entidades T almacenadas POR ESTADO ACTIVO
     *
     * @return Lista de las entidades almacenadas
     */
    @Override
    public List<T> sincronizados() {
        List<T> todos = null;
        try {
            Where<T, Id> where = dao.queryBuilder().where();
            where.eq("sincronizado",true);
            todos = where.query();
        } catch (SQLException e) {
            Log.e(TAG_LOG,
                    String.format("Un error ocurrió recuperando todas las entidades {%s}", clazz),
                    e);
        }
        return todos;
    }

    /**
     * Elimina una entidad T dado su Id. Retorna la entidad eliminada
     *
     * @param id
     *         Id de la entidad a eliminar
     *
     * @return El elemento T eliminado
     */
    @Override
    public T eliminar(Id id) {
        T entidad = buscarPorId(id);
        try {
            if (entidad != null) {
                dao.deleteById(id);
            }
        } catch (SQLException e) {
            Log.e(TAG_LOG, String.format(
                    "Un error ha ocurrido mientras se eliminaba la entidad {%s} con Id {%s}", clazz,
                    id), e);
        }
        return entidad;
    }
    /** Elimina todos los registros de la tabla **/
    @Override
    public void eliminarTodo() {
        try {
            dao.deleteBuilder().delete();
        } catch (SQLException e) {
            Log.e(TAG_LOG, String.format("Un error ha ocurrido limpiando la tabla de la entidad %s",
                    this.clazz), e);
        }
    }

    /**
     * update
     * @param entidad
     * @return
     */
    public boolean actualizar(T entidad) {
        boolean creado = false;

        try {
            dao.update(entidad);
            creado = true;
        } catch (SQLException e) {
            Log.e(TAG_LOG,
                    String.format("Un error ocurrió mientras se actualizaba la entidad {%s}", clazz), e);
        }

        return creado;
    }

    /**
     * Retorna el DAO de la entidad
     *
     * @return DAO de la entidad
     */
    @Override
    public Dao<T, Id> getDao() {
        return dao;
    }

    @Override
    public String toString() {
        return clazz.toString();
    }



}
