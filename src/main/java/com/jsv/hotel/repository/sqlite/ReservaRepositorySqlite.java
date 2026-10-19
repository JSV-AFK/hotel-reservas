package com.jsv.hotel.repository.sqlite;

import com.jsv.hotel.exception.PersistenciaException;
import com.jsv.hotel.model.EstadoReserva;
import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.model.Reserva;
import com.jsv.hotel.repository.ReservaRepository;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservaRepositorySqlite implements ReservaRepository {

    /** Trae la reserva junto con su habitación y su huésped en una sola consulta. */
    private static final String SELECT_BASE = """
            SELECT r.id, r.check_in, r.check_out, r.numero_huespedes, r.estado,
                   h.numero AS h_numero, h.tipo AS h_tipo,
                   h.precio_base AS h_precio_base, h.con_balcon AS h_con_balcon,
                   g.id AS g_id, g.nombre AS g_nombre, g.documento AS g_documento,
                   g.correo AS g_correo, g.telefono AS g_telefono
            FROM reservas r
            JOIN habitaciones h ON h.numero = r.habitacion_numero
            JOIN huespedes g ON g.id = r.huesped_id
            """;

    private final BaseDeDatos bd;

    public ReservaRepositorySqlite(BaseDeDatos bd) {
        this.bd = bd;
    }

    @Override
    public Reserva guardar(Reserva reserva) {
        if (reserva.getHuesped().getId() == null) {
            throw new IllegalStateException("El huésped debe guardarse antes que la reserva");
        }
        try (Connection conexion = bd.abrir()) {
            if (reserva.getId() == null) {
                insertar(conexion, reserva);
            } else {
                actualizar(conexion, reserva);
            }
            return reserva;
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la reserva", e);
        }
    }

    private void insertar(Connection conexion, Reserva reserva) throws SQLException {
        String sql = """
                INSERT INTO reservas (habitacion_numero, huesped_id, check_in, check_out,
                                      numero_huespedes, estado)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            asignarParametros(ps, reserva);
            ps.executeUpdate();
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    reserva.setId(claves.getLong(1));
                }
            }
        }
    }

    private void actualizar(Connection conexion, Reserva reserva) throws SQLException {
        String sql = """
                UPDATE reservas
                SET habitacion_numero = ?, huesped_id = ?, check_in = ?, check_out = ?,
                    numero_huespedes = ?, estado = ?
                WHERE id = ?
                """;
        try (PreparedStatement ps = conexion.prepareStatement(sql)) {
            asignarParametros(ps, reserva);
            ps.setLong(7, reserva.getId());
            ps.executeUpdate();
        }
    }

    private static void asignarParametros(PreparedStatement ps, Reserva reserva) throws SQLException {
        ps.setInt(1, reserva.getHabitacion().getNumero());
        ps.setLong(2, reserva.getHuesped().getId());
        ps.setString(3, reserva.getCheckIn().toString());
        ps.setString(4, reserva.getCheckOut().toString());
        ps.setInt(5, reserva.getNumeroHuespedes());
        ps.setString(6, reserva.getEstado().name());
    }

    @Override
    public Optional<Reserva> buscarPorId(long id) {
        try (Connection conexion = bd.abrir();
             PreparedStatement ps = conexion.prepareStatement(SELECT_BASE + " WHERE r.id = ?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(leer(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo consultar la reserva " + id, e);
        }
    }

    @Override
    public List<Reserva> listarPorHabitacion(int numeroHabitacion) {
        String sql = SELECT_BASE + " WHERE r.habitacion_numero = ? ORDER BY r.check_in, r.id";
        try (Connection conexion = bd.abrir(); PreparedStatement ps = conexion.prepareStatement(sql)) {
            ps.setInt(1, numeroHabitacion);
            try (ResultSet rs = ps.executeQuery()) {
                return leerTodas(rs);
            }
        } catch (SQLException e) {
            throw new PersistenciaException(
                    "No se pudieron consultar las reservas de la habitación " + numeroHabitacion, e);
        }
    }

    @Override
    public List<Reserva> listar() {
        try (Connection conexion = bd.abrir();
             PreparedStatement ps = conexion.prepareStatement(SELECT_BASE + " ORDER BY r.id");
             ResultSet rs = ps.executeQuery()) {
            return leerTodas(rs);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron listar las reservas", e);
        }
    }

    private static List<Reserva> leerTodas(ResultSet rs) throws SQLException {
        List<Reserva> resultado = new ArrayList<>();
        while (rs.next()) {
            resultado.add(leer(rs));
        }
        return resultado;
    }

    private static Reserva leer(ResultSet rs) throws SQLException {
        Habitacion habitacion = Mapeo.habitacion(
                rs.getInt("h_numero"),
                rs.getString("h_tipo"),
                rs.getString("h_precio_base"),
                rs.getInt("h_con_balcon") == 1);
        Huesped huesped = Mapeo.huesped(
                rs.getLong("g_id"),
                rs.getString("g_nombre"),
                rs.getString("g_documento"),
                rs.getString("g_correo"),
                rs.getString("g_telefono"));

        return Reserva.reconstruir(
                rs.getLong("id"),
                habitacion,
                huesped,
                LocalDate.parse(rs.getString("check_in")),
                LocalDate.parse(rs.getString("check_out")),
                rs.getInt("numero_huespedes"),
                EstadoReserva.valueOf(rs.getString("estado")));
    }
}