package com.jsv.hotel;

import com.jsv.hotel.exception.HotelException;
import com.jsv.hotel.model.*;
import com.jsv.hotel.pricing.*;
import com.jsv.hotel.repository.HabitacionRepository;
import com.jsv.hotel.repository.memoria.*;
import com.jsv.hotel.service.Cancelacion;
import com.jsv.hotel.service.ReservaService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class App {

    public static void main(String[] args) {
        HabitacionRepository habitaciones = new HabitacionRepositoryMemoria();
        habitaciones.guardar(new HabitacionSencilla(101, BigDecimal.valueOf(100_000)));
        habitaciones.guardar(new HabitacionDoble(201, BigDecimal.valueOf(150_000), true));
        habitaciones.guardar(new Suite(301, BigDecimal.valueOf(300_000)));

        ReservaService servicio = new ReservaService(
                habitaciones,
                new HuespedRepositoryMemoria(),
                new ReservaRepositoryMemoria(),
                new CalculadoraPrecio(List.of(new DescuentoEstanciaLarga(), new DescuentoTemporadaBaja())),
                new PoliticaCancelacion(),
                Clock.systemDefaultZone());

        LocalDate entrada = LocalDate.now().plusDays(30);
        LocalDate salida = entrada.plusDays(8);
        Huesped ana = new Huesped("Ana Gómez", "1020304050", "ana@gmail.com", "3001234567");

        System.out.println("=== Habitaciones disponibles para 3 huéspedes ===");
        servicio.buscarDisponibles(entrada, salida, 3)
                .forEach(h -> System.out.println(h + " - $" + h.calcularPrecioPorNoche().longValue() + " por noche"));

        Reserva reserva = servicio.reservar(301, ana, entrada, salida, 3);
        Cotizacion cotizacion = servicio.cotizar(reserva);

        System.out.println("\n=== Reserva " + reserva.getId() + " ===");
        System.out.println("Huésped:   " + reserva.getHuesped());
        System.out.println("Habitación: " + reserva.getHabitacion());
        System.out.println("Fechas:    " + entrada + " a " + salida + " (" + reserva.getNoches() + " noches)");
        System.out.printf("Subtotal:  $%,d%n", cotizacion.subtotal().longValue());
        System.out.printf("Descuento: %s (%s%%) -$%,d%n", cotizacion.descuento(),
                cotizacion.porcentaje(), cotizacion.valorDescuento().longValue());
        System.out.printf("Total:     $%,d%n", cotizacion.total().longValue());

        System.out.println("\n=== Intento de reservar la misma suite en fechas cruzadas ===");
        try {
            servicio.reservar(301, ana, entrada.plusDays(2), salida.plusDays(2), 2);
        } catch (HotelException e) {
            System.out.println("Rechazada: " + e.getMessage());
        }

        Cancelacion cancelacion = servicio.cancelar(reserva.getId());
        System.out.println("\n=== Cancelación ===");
        System.out.println("Estado:       " + cancelacion.reserva().getEstado());
        System.out.printf("Penalización: $%,d%n", cancelacion.penalizacion().longValue());
    }
}