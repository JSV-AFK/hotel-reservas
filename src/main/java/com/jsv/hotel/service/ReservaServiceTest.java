package com.jsv.hotel.service;

import static org.junit.jupiter.api.Assertions.*;

import com.jsv.hotel.exception.EstadoReservaInvalidoException;
import com.jsv.hotel.exception.HabitacionNoDisponibleException;
import com.jsv.hotel.exception.RecursoNoEncontradoException;
import com.jsv.hotel.exception.ReservaInvalidaException;
import com.jsv.hotel.model.EstadoReserva;
import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.model.HabitacionDoble;
import com.jsv.hotel.model.HabitacionSencilla;
import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.model.Reserva;
import com.jsv.hotel.model.Suite;
import com.jsv.hotel.pricing.CalculadoraPrecio;
import com.jsv.hotel.pricing.Cotizacion;
import com.jsv.hotel.pricing.DescuentoEstanciaLarga;
import com.jsv.hotel.pricing.DescuentoTemporadaBaja;
import com.jsv.hotel.pricing.PoliticaCancelacion;
import com.jsv.hotel.repository.HabitacionRepository;
import com.jsv.hotel.repository.memoria.HabitacionRepositoryMemoria;
import com.jsv.hotel.repository.memoria.HuespedRepositoryMemoria;
import com.jsv.hotel.repository.memoria.ReservaRepositoryMemoria;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservaServiceTest {

    // "Hoy" es siempre el 19 de octubre de 2026 en las pruebas.
    private final Clock reloj =
            Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneId.of("America/Bogota"));

    private ReservaService servicio;

    @BeforeEach
    void preparar() {
        HabitacionRepository habitaciones = new HabitacionRepositoryMemoria();
        habitaciones.guardar(new HabitacionSencilla(101, cop(100_000)));
        habitaciones.guardar(new HabitacionDoble(201, cop(150_000), false));
        habitaciones.guardar(new Suite(301, cop(300_000)));

        servicio = new ReservaService(
                habitaciones,
                new HuespedRepositoryMemoria(),
                new ReservaRepositoryMemoria(),
                new CalculadoraPrecio(List.of(new DescuentoEstanciaLarga(), new DescuentoTemporadaBaja())),
                new PoliticaCancelacion(),
                reloj);
    }

    // ---------- Ayudas ----------

    private static BigDecimal cop(long valor) {
        return BigDecimal.valueOf(valor);
    }

    private static LocalDate oct(int dia) {
        return LocalDate.of(2026, 10, dia);
    }

    private static LocalDate dic(int dia) {
        return LocalDate.of(2026, 12, dia);
    }

    private static Huesped ana() {
        return new Huesped("Ana Gómez", "123", "ana@gmail.com", "");
    }

    /** Reserva la 101, la cancela y devuelve la penalización en pesos. */
    private long penalizacion(LocalDate entrada, LocalDate salida) {
        Reserva reserva = servicio.reservar(101, ana(), entrada, salida, 1);
        return servicio.cancelar(reserva.getId()).penalizacion().longValueExact();
    }

    // ---------- Reservas y disponibilidad ----------

    @Test
    void creaLaReservaConIdYEstadoConfirmada() {
        Reserva reserva = servicio.reservar(101, ana(), dic(10), dic(15), 1);

        assertNotNull(reserva.getId());
        assertNotNull(reserva.getHuesped().getId());
        assertEquals(EstadoReserva.CONFIRMADA, reserva.getEstado());
    }

    @Test
    void rechazaReservasQueSeSolapanEnLaMismaHabitacion() {
        servicio.reservar(101, ana(), dic(10), dic(15), 1);

        assertThrows(HabitacionNoDisponibleException.class,
                () -> servicio.reservar(101, ana(), dic(12), dic(14), 1));
        assertThrows(HabitacionNoDisponibleException.class,
                () -> servicio.reservar(101, ana(), dic(14), dic(18), 1));

        // Entrar el día que otro sale, y salir el día que otro entra, está permitido.
        assertNotNull(servicio.reservar(101, ana(), dic(15), dic(18), 1));
        assertNotNull(servicio.reservar(101, ana(), dic(8), dic(10), 1));
    }

    @Test
    void cancelarLiberaLaHabitacion() {
        Reserva reserva = servicio.reservar(101, ana(), dic(10), dic(15), 1);
        servicio.cancelar(reserva.getId());

        assertNotNull(servicio.reservar(101, ana(), dic(10), dic(15), 1));
    }

    @Test
    void buscaHabitacionesDisponiblesPorFechasYCapacidad() {
        servicio.reservar(101, ana(), dic(10), dic(15), 1);

        List<Integer> paraUno = servicio.buscarDisponibles(dic(12), dic(14), 1).stream()
                .map(Habitacion::getNumero).toList();
        List<Integer> paraTres = servicio.buscarDisponibles(dic(12), dic(14), 3).stream()
                .map(Habitacion::getNumero).toList();
        List<Integer> otroPeriodo = servicio.buscarDisponibles(dic(20), dic(22), 1).stream()
                .map(Habitacion::getNumero).toList();

        assertEquals(List.of(201, 301), paraUno);
        assertEquals(List.of(301), paraTres);
        assertEquals(List.of(101, 201, 301), otroPeriodo);
    }

    @Test
    void rechazaFechasEnElPasadoOIncoherentes() {
        assertThrows(ReservaInvalidaException.class,
                () -> servicio.reservar(101, ana(), oct(10), oct(12), 1));
        assertThrows(ReservaInvalidaException.class,
                () -> servicio.reservar(101, ana(), dic(15), dic(10), 1));
    }

    @Test
    void rechazaUnaHabitacionQueNoExiste() {
        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.reservar(999, ana(), dic(10), dic(12), 1));
    }

    @Test
    void reutilizaAlHuespedConElMismoDocumento() {
        Reserva primera = servicio.reservar(101, ana(), dic(10), dic(12), 1);
        Reserva segunda = servicio.reservar(201, ana(), dic(10), dic(12), 2);

        assertEquals(primera.getHuesped().getId(), segunda.getHuesped().getId());
    }

    // ---------- Precios y descuentos ----------

    @Test
    void aplicaDescuentoPorEstanciaLarga() {
        Cotizacion semana = servicio.cotizar(servicio.reservar(201, ana(), dic(1), dic(8), 2));
        assertEquals(1_050_000, semana.subtotal().longValueExact());
        assertEquals(105_000, semana.valorDescuento().longValueExact());
        assertEquals(945_000, semana.total().longValueExact());

        Cotizacion quincena = servicio.cotizar(servicio.reservar(301, ana(), dic(1), dic(15), 2));
        assertEquals(4_760_000, quincena.subtotal().longValueExact());
        assertEquals(714_000, quincena.valorDescuento().longValueExact());
        assertEquals(4_046_000, quincena.total().longValueExact());
    }

    @Test
    void aplicaDescuentoPorTemporadaBaja() {
        Cotizacion cotizacion = servicio.cotizar(servicio.reservar(101, ana(), oct(26), oct(29), 1));

        assertEquals("Temporada baja", cotizacion.descuento());
        assertEquals(300_000, cotizacion.subtotal().longValueExact());
        assertEquals(30_000, cotizacion.valorDescuento().longValueExact());
        assertEquals(270_000, cotizacion.total().longValueExact());
    }

    @Test
    void aplicaElMayorDescuentoSinAcumularlos() {
        // 14 noches en octubre: estancia larga (15 %) gana a temporada baja (10 %).
        Cotizacion cotizacion = servicio.cotizar(
                servicio.reservar(101, ana(), oct(26), LocalDate.of(2026, 11, 9), 1));

        assertEquals("Estancia larga", cotizacion.descuento());
        assertEquals(1_400_000, cotizacion.subtotal().longValueExact());
        assertEquals(210_000, cotizacion.valorDescuento().longValueExact());
        assertEquals(1_190_000, cotizacion.total().longValueExact());
    }

    // ---------- Cancelaciones ----------

    @Test
    void noCobraPenalizacionConSieteDiasOMasDeAnticipacion() {
        assertEquals(0, penalizacion(oct(26), oct(28))); // exactamente 7 días
        assertEquals(0, penalizacion(dic(1), dic(3)));
    }

    @Test
    void cobraUnaNocheConEntreDosYSeisDiasDeAnticipacion() {
        assertEquals(100_000, penalizacion(oct(25), oct(27))); // 6 días
        assertEquals(100_000, penalizacion(oct(21), oct(23))); // 2 días
    }

    @Test
    void cobraLaMitadDelTotalConMenosDeDosDias() {
        assertEquals(90_000, penalizacion(oct(20), oct(22))); // 1 día
        assertEquals(90_000, penalizacion(oct(19), oct(21))); // el mismo día
    }

    @Test
    void laPenalizacionNuncaSuperaElTotal() {
        // Una noche con 10 % de descuento: total 90.000, menor que la primera noche.
        assertEquals(90_000, penalizacion(oct(25), oct(26)));
    }

    @Test
    void noPermiteCancelarDosVeces() {
        Reserva reserva = servicio.reservar(101, ana(), dic(10), dic(12), 1);
        servicio.cancelar(reserva.getId());

        assertThrows(EstadoReservaInvalidoException.class, () -> servicio.cancelar(reserva.getId()));
    }

    @Test
    void falllaAlCancelarUnaReservaQueNoExiste() {
        assertThrows(RecursoNoEncontradoException.class, () -> servicio.cancelar(999));
    }

    // ---------- Check-in y check-out ----------

    @Test
    void noPermiteCheckInAntesDeLaFechaDeEntrada() {
        Reserva reserva = servicio.reservar(101, ana(), dic(10), dic(12), 1);

        assertThrows(ReservaInvalidaException.class, () -> servicio.hacerCheckIn(reserva.getId()));
    }

    @Test
    void completaElCicloDeCheckInYCheckOut() {
        Reserva reserva = servicio.reservar(101, ana(), oct(19), oct(21), 1);

        assertEquals(EstadoReserva.EN_CURSO, servicio.hacerCheckIn(reserva.getId()).getEstado());
        assertEquals(EstadoReserva.FINALIZADA, servicio.hacerCheckOut(reserva.getId()).getEstado());
    }
}