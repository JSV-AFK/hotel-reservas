package com.jsv.hotel;

import com.jsv.hotel.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class App {

    public static void main(String[] args) {
        List<Habitacion> habitaciones = List.of(
                new HabitacionSencilla(101, BigDecimal.valueOf(100_000)),
                new HabitacionDoble(201, BigDecimal.valueOf(150_000), true),
                new Suite(301, BigDecimal.valueOf(300_000)));

        System.out.println("=== Precios por noche ===");
        for (Habitacion h : habitaciones) {
            System.out.printf("%-22s $%,d%n", h, h.calcularPrecioPorNoche().longValue());
        }

        Huesped huesped = new Huesped("Ana Gómez", "1020304050", "ana@gmail.com", "3001234567");
        Reserva reserva = new Reserva(habitaciones.get(2), huesped,
                LocalDate.of(2026, 12, 20), LocalDate.of(2026, 12, 24), 3);

        System.out.println("\n=== Reserva ===");
        System.out.println("Huésped:  " + reserva.getHuesped());
        System.out.println("Hab.:     " + reserva.getHabitacion());
        System.out.println("Noches:   " + reserva.getNoches());
        System.out.printf("Subtotal: $%,d%n", reserva.calcularSubtotal().longValue());
        System.out.println("Estado:   " + reserva.getEstado());
    }
}