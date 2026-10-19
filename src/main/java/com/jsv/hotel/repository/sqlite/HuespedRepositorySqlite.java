package com.jsv.hotel.repository.sqlite;

import com.jsv.hotel.exception.PersistenciaException;
import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.repository.HuespedRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Optional;

public class HuespedRepositorySqlite implements HuespedRepository {

    private final BaseDeDatos bd;

    public HuespedRepositorySqlite(BaseDeDatos bd) {
        this.bd = bd;
    }

    @Override
    public Huesped guardar(Huesped huesped) {
        try (Connection conexion = bd.abrir()) {
            if (huesped.getId() == null) {
                insertar(conexion, huesped);
            } else {
                actualizar(conexion, huesped);
            }
            return huesped;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar al huésped " + huesped.getDocumento(), e);
        }
    }

    private void insertar(Connection conexion, Huesped huesped) throws SQLException {
        String sql = "INSERT INTO huespedes (nombre, documento, correo, telefono) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, huesped.getNombre());
            ps.setString(2, huesped.getDocumento());
            ps.setString(3, huesped.getCorreo());
            ps.setString(4, huesped.getTelefono());
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    huesped.setId(claves.getLong(1));
                }
            }
        }
    }

    private void actualizar(Connection conexion, Huesped huesped) throws SQLException {
        String sql = "UPDATE huespedes SET nombre = ?, documento = ?, correo = ?, telefono = ? WHERE id = ?";
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, huesped.getNombre());
            ps.setString(2, huesped.getDocumento());
            ps.setString(3, huesped.getCorreo());
            ps.setString(4, huesped.getTelefono());
            ps.setLong(5, huesped.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public Optional<Huesped> buscarPorDocumento(String documento) {
        String sql = "SELECT id, nombre, documento, correo, telefono FROM huespedes WHERE documento = ?";
        try (Connection conexion = bd.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setString(1, documento);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                return Optional.of(Mapeo.huesped(
                        rs.getLong("id"),
                        rs.getString("nombre"),
                        rs.getString("documento"),
                        rs.getString("correo"),
                        rs.getString("telefono")));
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar al huésped " + documento, e);
        }
    }
}