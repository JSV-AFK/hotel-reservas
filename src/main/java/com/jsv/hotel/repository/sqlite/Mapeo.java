package com.jsv.hotel.repository.sqlite;

import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.model.HabitacionDoble;
import com.jsv.hotel.model.HabitacionSencilla;
import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.model.Suite;
import java.math.BigDecimal;

/** Traduce entre el modelo de dominio y los valores guardados en las tablas. */
final class Mapeo {

    private Mapeo() {
    }

    static String codigoTipo(Habitacion habitacion) {
        if (habitacion instanceof Suite) {
            return "SUITE";
        }
        if (habitacion instanceof HabitacionDoble) {
            return "DOBLE";
        }
        if (habitacion instanceof HabitacionSencilla) {
            return "SENCILLA";
        }
        throw new IllegalArgumentException(
                "Tipo de habitación no soportado: " + habitacion.getClass().getSimpleName());
    }

    static boolean conBalcon(Habitacion habitacion) {
        return habitacion instanceof HabitacionDoble doble && doble.tieneBalcon();
    }

    static Habitacion habitacion(int numero, String tipo, String precioBase, boolean conBalcon) {
        BigDecimal precio = new BigDecimal(precioBase);
        return switch (tipo) {
            case "SENCILLA" -> new HabitacionSencilla(numero, precio);
            case "DOBLE" -> new HabitacionDoble(numero, precio, conBalcon);
            case "SUITE" -> new Suite(numero, precio);
            default -> throw new IllegalStateException("Tipo de habitación desconocido en la base: " + tipo);
        };
    }

    static Huesped huesped(long id, String nombre, String documento, String correo, String telefono) {
        Huesped huesped = new Huesped(nombre, documento, correo, telefono);
        huesped.setId(id);
        return huesped;
    }
}