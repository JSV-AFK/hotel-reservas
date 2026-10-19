package com.jsv.hotel.repository.sqlite;

import static org.junit.jupiter.api.Assertions.*;

import com.jsv.hotel.exception.HabitacionNoDisponibleException;
import com.jsv.hotel.exception.PersistenciaException;
import com.jsv.hotel.model.EstadoReserva;
import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.model.HabitacionDoble;
import com.jsv.hotel.model.HabitacionSencilla;
import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.model.Reserva;
import com.jsv.hotel.model.Suite;
import com.jsv.hotel.pricing.CalculadoraPrecio;
import com.jsv.hotel.pricing.DescuentoEstanciaLarga;
import com.jsv.hotel.pricing.DescuentoTemporadaBaja;
import com.jsv.hotel.pricing.PoliticaCancelacion;
import com.jsv.hotel.repository.HabitacionRepository;
import com.jsv.hotel.repository.HuespedRepository;
import com.jsv.hotel.repository.ReservaRepository;
import com.jsv.hotel.service.Cancelacion;
import com.jsv.hotel.service.ReservaService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SqliteRepositoriosTest {

    @TempDir
    Path carpeta;

    private BaseDeDatos bd;
    private HabitacionRepository habitaciones;
    private HuespedRepository huespedes;
    private ReservaRepository reservas;
    private Huesped ana;

    @BeforeEach
    void preparar() {
        bd = abrirBase();
        habitaciones = new HabitacionRepositorySqlite(bd);
        huespedes = new HuespedRepositorySqlite(bd);
        reservas = new ReservaRepositorySqlite(bd);

        habitaciones.guardar(new HabitacionSencilla(101, cop(100_000)));
        habitaciones.guardar(new HabitacionDoble(201, cop(150_000), false));
        ana = huespedes.guardar(new Huesped("Ana Gómez", "123", "ana@gmail.com", "3001234567"));
    }

    // ---------- Ayudas ----------

    private BaseDeDatos abrirBase() {
        return new BaseDeDatos(carpeta.resolve("prueba.db").toString());
    }

    private static BigDecimal cop(long valor) {
        return BigDecimal.valueOf(valor);
    }

    private static LocalDate dic(int dia) {
        return LocalDate.of(2026, 12, dia);
    }

    private Reserva reservaEn(int numeroHabitacion, LocalDate entrada, LocalDate salida) {
        Habitacion habitacion = habitaciones.buscarPorNumero(numeroHabitacion).orElseThrow();
        return new Reserva(habitacion, ana, entrada, salida, 1);
    }

    private static ReservaService crearServicio(BaseDeDatos base) {
        Clock reloj = Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneId.of("America/Bogota"));
        return new ReservaService(
                new HabitacionRepositorySqlite(base),
                new HuespedRepositorySqlite(base),
                new ReservaRepositorySqlite(base),
                new CalculadoraPrecio(List.of(new DescuentoEstanciaLarga(), new DescuentoTemporadaBaja())),
                new PoliticaCancelacion(),
                reloj);
    }

    // ---------- Habitaciones ----------

    @Test
    void guardaYRecuperaLosTresTiposDeHabitacion() {
        habitaciones.guardar(new Suite(301, cop(300_000)));
        habitaciones.guardar(new HabitacionDoble(202, cop(150_000), true));

        List<Habitacion> lista = habitaciones.listar();

        assertEquals(List.of(101, 201, 202, 301), lista.stream().map(Habitacion::getNumero).toList());
        assertInstanceOf(HabitacionSencilla.class, lista.get(0));
        assertInstanceOf(HabitacionDoble.class, lista.get(1));
        assertInstanceOf(Suite.class, lista.get(3));
        assertEquals(150_000, lista.get(1).calcularPrecioPorNoche().longValueExact()); // sin balcón
        assertEquals(165_000, lista.get(2).calcularPrecioPorNoche().longValueExact()); // con balcón
        assertEquals(340_000, lista.get(3).calcularPrecioPorNoche().longValueExact()); // suite + desayuno
    }

    @Test
    void guardarUnaHabitacionExistenteLaActualiza() {
        habitaciones.guardar(new HabitacionSencilla(101, cop(120_000)));

        assertEquals(2, habitaciones.listar().size());
        assertEquals(120_000, habitaciones.buscarPorNumero(101).orElseThrow().getPrecioBase().longValueExact());
        assertTrue(habitaciones.buscarPorNumero(999).isEmpty());
    }

    // ---------- Huéspedes ----------

    @Test
    void guardaAlHuespedConIdYLoBuscaPorDocumento() {
        assertNotNull(ana.getId());

        Huesped leido = huespedes.buscarPorDocumento("123").orElseThrow();
        assertEquals(ana.getId(), leido.getId());
        assertEquals("Ana Gómez", leido.getNombre());
        assertEquals("ana@gmail.com", leido.getCorreo());
        assertEquals("3001234567", leido.getTelefono());
        assertTrue(huespedes.buscarPorDocumento("999").isEmpty());
    }

    // ---------- Reservas ----------

    @Test
    void guardaYRecuperaUnaReservaConSuEstado() {
        Reserva guardada = reservas.guardar(reservaEn(201, dic(10), dic(15)));
        assertNotNull(guardada.getId());

        Reserva leida = reservas.buscarPorId(guardada.getId()).orElseThrow();
        assertEquals(EstadoReserva.CONFIRMADA, leida.getEstado());
        assertEquals(dic(10), leida.getCheckIn());
        assertEquals(dic(15), leida.getCheckOut());
        assertEquals(201, leida.getHabitacion().getNumero());
        assertEquals("Ana Gómez", leida.getHuesped().getNombre());

        leida.hacerCheckIn();
        reservas.guardar(leida);

        assertEquals(EstadoReserva.EN_CURSO, reservas.buscarPorId(guardada.getId()).orElseThrow().getEstado());
        assertTrue(reservas.buscarPorId(999).isEmpty());
    }

    @Test
    void listaLasReservasPorHabitacion() {
        reservas.guardar(reservaEn(101, dic(1), dic(3)));
        reservas.guardar(reservaEn(201, dic(1), dic(3)));
        reservas.guardar(reservaEn(101, dic(10), dic(12)));

        assertEquals(2, reservas.listarPorHabitacion(101).size());
        assertEquals(1, reservas.listarPorHabitacion(201).size());
        assertEquals(0, reservas.listarPorHabitacion(301).size());
        assertEquals(3, reservas.listar().size());
    }

    @Test
    void losDatosSobrevivenAlReabrirLaBase() {
        Reserva guardada = reservas.guardar(reservaEn(101, dic(1), dic(3)));

        BaseDeDatos reabierta = abrirBase();

        List<Reserva> otraSesion = new ReservaRepositorySqlite(reabierta).listar();
        assertEquals(1, otraSesion.size());
        assertEquals(guardada.getId(), otraSesion.get(0).getId());
        assertEquals(2, new HabitacionRepositorySqlite(reabierta).listar().size());
    }

    @Test
    void laBaseRechazaUnaReservaDeUnaHabitacionQueNoExiste() {
        // La habitación 301 nunca se guardó: la clave foránea debe impedirlo.
        Reserva sinHabitacion = new Reserva(new Suite(301, cop(300_000)), ana, dic(1), dic(3), 1);

        assertThrows(PersistenciaException.class, () -> reservas.guardar(sinHabitacion));
    }

    // ---------- El servicio sobre SQLite ----------

    @Test
    void elServicioFuncionaIgualSobreSqliteYLosDatosPersisten() {
        Reserva reserva = crearServicio(bd).reservar(101, ana(), dic(10), dic(15), 1);
        assertNotNull(reserva.getId());

        // Se "reinicia" la aplicación: nueva conexión y nuevo servicio sobre el mismo archivo.
        ReservaService reiniciado = crearServicio(abrirBase());

        assertThrows(HabitacionNoDisponibleException.class,
                () -> reiniciado.reservar(101, ana(), dic(12), dic(14), 1));

        Cancelacion cancelacion = reiniciado.cancelar(reserva.getId());
        assertEquals(EstadoReserva.CANCELADA, cancelacion.reserva().getEstado());
        assertNotNull(reiniciado.reservar(101, ana(), dic(12), dic(14), 1));
    }

    private static Huesped ana() {
        return new Huesped("Ana Gómez", "123", "ana@gmail.com", "");
    }
}