package com.jsv.hotel.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;

class HabitacionTest {

    private static BigDecimal cop(long valor) {
        return BigDecimal.valueOf(valor);
    }

    @Test
    void cadaTipoCalculaSuPrecioPorNoche() {
        List<Habitacion> habitaciones = List.of(
                new HabitacionSencilla(101, cop(100_000)),
                new HabitacionDoble(201, cop(150_000), false),
                new HabitacionDoble(202, cop(150_000), true),
                new Suite(301, cop(300_000)));

        List<Long> precios = habitaciones.stream()
                .map(h -> h.calcularPrecioPorNoche().longValueExact())
                .toList();

        assertEquals(List.of(100_000L, 150_000L, 165_000L, 340_000L), precios);
    }

    @Test
    void laCapacidadDependeDelTipo() {
        assertTrue(new HabitacionSencilla(101, cop(100_000)).admite(1));
        assertFalse(new HabitacionSencilla(101, cop(100_000)).admite(2));
        assertTrue(new HabitacionDoble(201, cop(150_000), false).admite(2));
        assertTrue(new Suite(301, cop(300_000)).admite(4));
        assertFalse(new Suite(301, cop(300_000)).admite(5));
        assertFalse(new Suite(301, cop(300_000)).admite(0));
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new HabitacionSencilla(0, cop(100_000)));
        assertThrows(IllegalArgumentException.class, () -> new HabitacionSencilla(101, cop(0)));
        assertThrows(IllegalArgumentException.class, () -> new Suite(301, null));
    }

    @Test
    void dosHabitacionesConElMismoNumeroSonIguales() {
        assertEquals(new HabitacionSencilla(101, cop(100_000)), new HabitacionSencilla(101, cop(120_000)));
    }
}