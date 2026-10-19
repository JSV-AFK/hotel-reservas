package com.jsv.hotel.model;

import static org.junit.jupiter.api.Assertions.*;

import com.jsv.hotel.exception.EstadoReservaInvalidoException;
import com.jsv.hotel.exception.ReservaInvalidaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ReservaTest {

    private final Habitacion doble = new HabitacionDoble(201, BigDecimal.valueOf(150_000), false);
    private final Huesped huesped = new Huesped("Ana Gómez", "123", "ana@gmail.com", "");

    private static LocalDate nov(int dia) {
        return LocalDate.of(2026, 11, dia);
    }

    private Reserva reserva(LocalDate entrada, LocalDate salida) {
        return new Reserva(doble, huesped, entrada, salida, 2);
    }

    @Test
    void calculaNochesYSubtotal() {
        Reserva r = reserva(nov(10), nov(13));
        assertEquals(3, r.getNoches());
        assertEquals(0, BigDecimal.valueOf(450_000).compareTo(r.calcularSubtotal()));
    }

    @Test
    void rechazaFechasIncoherentes() {
        assertThrows(ReservaInvalidaException.class, () -> reserva(nov(13), nov(10)));
        assertThrows(ReservaInvalidaException.class, () -> reserva(nov(10), nov(10)));
    }

    @Test
    void rechazaMasHuespedesQueLaCapacidad() {
        assertThrows(ReservaInvalidaException.class,
                () -> new Reserva(doble, huesped, nov(10), nov(12), 3));
    }

    @Test
    void detectaSolapamientos() {
        Reserva r = reserva(nov(10), nov(15));

        assertTrue(r.solapaCon(nov(12), nov(18)));  // se cruza al final
        assertTrue(r.solapaCon(nov(8), nov(11)));   // se cruza al inicio
        assertTrue(r.solapaCon(nov(11), nov(13)));  // queda dentro
        assertFalse(r.solapaCon(nov(15), nov(20))); // empieza el día que sale: permitido
        assertFalse(r.solapaCon(nov(5), nov(10)));  // termina el día que entra: permitido
    }

    @Test
    void recorreElCicloDeVidaCompleto() {
        Reserva r = reserva(nov(10), nov(12));
        assertEquals(EstadoReserva.CONFIRMADA, r.getEstado());

        r.hacerCheckIn();
        assertEquals(EstadoReserva.EN_CURSO, r.getEstado());

        r.hacerCheckOut();
        assertEquals(EstadoReserva.FINALIZADA, r.getEstado());
        assertFalse(r.getEstado().ocupaHabitacion());
    }

    @Test
    void sePuedeCancelarSoloSiEstaConfirmada() {
        Reserva r = reserva(nov(10), nov(12));
        r.cancelar();
        assertEquals(EstadoReserva.CANCELADA, r.getEstado());
        assertFalse(r.getEstado().ocupaHabitacion());

        Reserva enCurso = reserva(nov(10), nov(12));
        enCurso.hacerCheckIn();
        assertThrows(EstadoReservaInvalidoException.class, enCurso::cancelar);
    }

    @Test
    void noPermiteTransicionesInvalidas() {
        Reserva r = reserva(nov(10), nov(12));
        assertThrows(EstadoReservaInvalidoException.class, r::hacerCheckOut); // sin check-in
        r.hacerCheckIn();
        assertThrows(EstadoReservaInvalidoException.class, r::hacerCheckIn);  // ya está en curso
    }
}