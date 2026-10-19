package com.jsv.hotel.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class HuespedTest {

    @Test
    void normalizaLosDatos() {
        Huesped h = new Huesped("  Ana Gómez ", " 123 ", " ANA@Gmail.com ", null);
        assertEquals("Ana Gómez", h.getNombre());
        assertEquals("123", h.getDocumento());
        assertEquals("ana@gmail.com", h.getCorreo());
        assertEquals("", h.getTelefono());
    }

    @Test
    void rechazaDatosInvalidos() {
        assertThrows(IllegalArgumentException.class, () -> new Huesped(" ", "123", "a@b.com", ""));
        assertThrows(IllegalArgumentException.class, () -> new Huesped("Ana", "", "a@b.com", ""));
        assertThrows(IllegalArgumentException.class, () -> new Huesped("Ana", "123", "sin-arroba", ""));
    }
}