package com.rutero.rutero.util;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.j256.ormlite.android.AndroidConnectionSource;
import com.j256.ormlite.android.AndroidDatabaseConnection;
import com.j256.ormlite.android.DatabaseTableConfigUtil;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.dao.DaoManager;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;
import com.j256.ormlite.table.DatabaseTableConfig;
import com.j256.ormlite.table.TableUtils;
import com.rutero.rutero.data.model.usuario.Usuario;

@Singleton
public class BaseDeDatosHelper extends OrmLiteSqliteOpenHelper {

    private static final String DATABASE_NAME = "ruteroBdSql.db";
    private static final int DATABASE_VERSION = 3;
    private static BaseDeDatosHelper instance;

    /** Fuente de la conexión **/
    protected AndroidConnectionSource fuenteConexion = new AndroidConnectionSource(this);

    // Método para obtener la instancia única
    public static synchronized BaseDeDatosHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BaseDeDatosHelper(context.getApplicationContext());
        }
        return instance;
    }

    @Inject
    public BaseDeDatosHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        configBd(database);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        configBd(database, oldVersion, newVersion);
    }

    private void configBd(SQLiteDatabase db) {
        configBd(db, null, null);
    }

    private void configBd(SQLiteDatabase db, Integer oldVersion, Integer newVersion) {
        DatabaseConnection con = connectionSource.getSpecialConnection("Registro");
        boolean limpiarEspecial = false;
        if (con == null) {
            con = new AndroidDatabaseConnection(db, true);

            try {
                connectionSource.saveSpecialConnection(con);
                limpiarEspecial = true;
            } catch (java.sql.SQLException e) {
                throw new IllegalStateException("No se pudo guardar la conexión especial", e);
            }
        }

        try {
            if (oldVersion == null || newVersion == null) {
                onCreate();
            } else {
                onUpgrade(oldVersion, newVersion);
            }
        } finally {
            if (limpiarEspecial) {
                connectionSource.clearSpecialConnection(con);
            }
        }
    }

    /**
     * Crea el esquema de BD
     */
    private void onCreate() {
        createTable(Usuario.class);
    }

    public void createTable(Class<?> tableClass) {
        try {
            TableUtils.createTableIfNotExists(connectionSource, tableClass);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (java.sql.SQLException e) {
            e.printStackTrace();
        }
    }

    private void onUpgrade(int oldVersion, int newVersion) {
        try {
            Log.i(BaseDeDatosHelper.class.getName(), "onUpgrade");

            TableUtils.dropTable(connectionSource, Usuario.class, true);

            onCreate();
        } catch (java.sql.SQLException e) {
            Log.e(BaseDeDatosHelper.class.getName(), "No se puede eliminar la BD", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Este método obtiene un DAO dado
     * <p/>
     * Extraido de: https://goo.gl/6LIYy2
     *
     * @param clazz
     *         Instancia de la clase pedida para el DAO
     * @param <D>
     *         Super clase del DAO
     * @param <T>
     *         Clase pedida para el DAO
     *
     * @return El DAO
     *
     * @throws java.sql.SQLException
     */
    public <D extends Dao<T, ?>, T> D getDao(Class<T> clazz) throws java.sql.SQLException {
        // lookup the dao, possibly invoking the cached database config
        Dao<T, ?> dao = DaoManager.lookupDao(connectionSource, clazz);
        if (dao == null) {
            // try to use our new reflection magic
            DatabaseTableConfig<T>
                    tableConfig = DatabaseTableConfigUtil.fromClass(connectionSource, clazz);
            if (tableConfig == null) {
                /**
                 * TODO: we have to do this to get to see if they are using the deprecated
                 * annotations like
                 * {@link DatabaseFieldSimple}.
                 */
                dao = (Dao<T, ?>) DaoManager.createDao(connectionSource, clazz);
            } else {
                dao = (Dao<T, ?>) DaoManager.createDao(connectionSource, tableConfig);
            }
        }

        @SuppressWarnings("unchecked")
        D castDao = (D) dao;
        return castDao;
    }
}

