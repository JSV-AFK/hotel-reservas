package com.jsv.hotel.repository.sqlite;

import com.jsv.hotel.exception.PersistenciaException;
import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.repository.HabitacionRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HabitacionRepositorySqlite implements HabitacionRepository {

    private final BaseDeDatos bd;

    public HabitacionRepositorySqlite(BaseDeDatos bd) {
        this.bd = bd;
    }

    @Override
    public void guardar(Habitacion habitacion) {
        String sql = """
                INSERT INTO habitaciones (numero, tipo, precio_base, con_balcon)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (numero) DO UPDATE SET
                    tipo        = excluded.tipo,
                    precio_base = excluded.precio_base,
                    con_balcon  = excluded.con_balcon
                """;
        try (Connection conexion = bd.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, habitacion.getNumero());
            ps.setString(2, Mapeo.codigoTipo(habitacion));
            ps.setString(3, habitacion.getPrecioBase().toPlainString());
            ps.setInt(4, Mapeo.conBalcon(habitacion) ? 1 : 0);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la habitación " + habitacion.getNumero(), e);
        }
    }

    @Override
    public Optional<Habitacion> buscarPorNumero(int numero) {
        String sql = "SELECT numero, tipo, precio_base, con_balcon FROM habitaciones WHERE numero = ?";
        try (Connection conexion = bd.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, numero);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar la habitación " + numero, e);
        }
    }

    @Override
    public List<Habitacion> listar() {
        String sql = "SELECT numero, tipo, precio_base, con_balcon FROM habitaciones ORDER BY numero";
        try (Connection conexion = bd.abrir();
             PreparedStatement ps = conexion.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            List<Habitacion> resultado = new ArrayList<>();
            while (rs.next()) {
                resultado.add(leer(rs));
            }
            return resultado;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar las habitaciones", e);
        }
    }

    private static Habitacion leer(ResultSet rs) throws SQLException {
        return Mapeo.habitacion(
                rs.getInt("numero"),
                rs.getString("tipo"),
                rs.getString("precio_base"),
                rs.getInt("con_balcon") == 1);
    }
}