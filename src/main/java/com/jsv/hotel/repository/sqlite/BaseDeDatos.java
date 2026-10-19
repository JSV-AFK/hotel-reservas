package com.jsv.hotel.repository.sqlite;

import com.jsv.hotel.exception.PersistenciaException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class BaseDeDatos {

    private final String url;

    public BaseDeDatos(String rutaArchivo) {
        this.url = "jdbc:sqlite:" + rutaArchivo;
        crearEsquema();
    }

    /** Abre una conexión nueva. Quien la pida debe cerrarla (try-with-resources). */
    public Connection abrir() throws SQLException {
        Connection conexion = DriverManager.getConnection(url);
        try (Statement st = conexion.createStatement()) {
            // SQLite no aplica las claves foráneas a menos que se active en cada conexión.
            st.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException e) {
            conexion.close();
            throw e;
        }
        return conexion;
    }

    private void crearEsquema() {
        try (Connection conexion = abrir(); Statement st = conexion.createStatement()) {
            st.execute("""
                    CREATE TABLE IF NOT EXISTS habitaciones (
                        numero      INTEGER PRIMARY KEY,
                        tipo        TEXT NOT NULL CHECK (tipo IN ('SENCILLA', 'DOBLE', 'SUITE')),
                        precio_base TEXT NOT NULL,
                        con_balcon  INTEGER NOT NULL DEFAULT 0
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS huespedes (
                        id        INTEGER PRIMARY KEY AUTOINCREMENT,
                        nombre    TEXT NOT NULL,
                        documento TEXT NOT NULL UNIQUE,
                        correo    TEXT NOT NULL,
                        telefono  TEXT NOT NULL DEFAULT ''
                    )
                    """);
            st.execute("""
                    CREATE TABLE IF NOT EXISTS reservas (
                        id                INTEGER PRIMARY KEY AUTOINCREMENT,
                        habitacion_numero INTEGER NOT NULL REFERENCES habitaciones (numero),
                        huesped_id        INTEGER NOT NULL REFERENCES huespedes (id),
                        check_in          TEXT NOT NULL,
                        check_out         TEXT NOT NULL,
                        numero_huespedes  INTEGER NOT NULL,
                        estado            TEXT NOT NULL
                    )
                    """);
            st.execute("""
                    CREATE INDEX IF NOT EXISTS idx_reservas_habitacion
                        ON reservas (habitacion_numero, check_in, check_out)
                    """);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo crear el esquema de la base de datos", e);
        }
    }
}