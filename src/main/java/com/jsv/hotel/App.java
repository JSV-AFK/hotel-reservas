package com.jsv.hotel;

import com.jsv.hotel.exception.HotelException;
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
import com.jsv.hotel.repository.ReservaRepository;
import com.jsv.hotel.repository.sqlite.BaseDeDatos;
import com.jsv.hotel.repository.sqlite.HabitacionRepositorySqlite;
import com.jsv.hotel.repository.sqlite.HuespedRepositorySqlite;
import com.jsv.hotel.repository.sqlite.ReservaRepositorySqlite;
import com.jsv.hotel.service.ReservaService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class App {

    public static void main(String[] args) {
        BaseDeDatos bd = new BaseDeDatos("hotel.db");
        HabitacionRepository habitaciones = new HabitacionRepositorySqlite(bd);
        ReservaRepository reservas = new ReservaRepositorySqlite(bd);

        ReservaService servicio = new ReservaService(
                habitaciones,
                new HuespedRepositorySqlite(bd),
                reservas,
                new CalculadoraPrecio(List.of(new DescuentoEstanciaLarga(), new DescuentoTemporadaBaja())),
                new PoliticaCancelacion(),
                Clock.systemDefaultZone());

        if (habitaciones.listar().isEmpty()) {
            habitaciones.guardar(new HabitacionSencilla(101, BigDecimal.valueOf(100_000)));
            habitaciones.guardar(new HabitacionDoble(201, BigDecimal.valueOf(150_000), true));
            habitaciones.guardar(new Suite(301, BigDecimal.valueOf(300_000)));
            System.out.println("Base de datos nueva: se cargaron 3 habitaciones.");
        }

        System.out.println("Reservas guardadas al iniciar: " + reservas.listar().size());

        LocalDate entrada = LocalDate.now().plusDays(30);
        LocalDate salida = entrada.plusDays(3);
        Huesped ana = new Huesped("Ana Gómez", "1020304050", "ana@gmail.com", "3001234567");

        try {
            Reserva reserva = servicio.reservar(301, ana, entrada, salida, 3);
            Cotizacion cotizacion = servicio.cotizar(reserva);
            System.out.printf("Reserva #%d creada: %s, del %s al %s, total $%,d%n",
                    reserva.getId(), reserva.getHabitacion(), entrada, salida,
                    cotizacion.total().longValue());
        } catch (HotelException e) {
            System.out.println("No se pudo reservar: " + e.getMessage());
        }

        System.out.println("\nReservas en la base de datos:");
        for (Reserva r : reservas.listar()) {
            System.out.printf("#%d  %s  %s -> %s  [%s]%n",
                    r.getId(), r.getHabitacion(), r.getCheckIn(), r.getCheckOut(), r.getEstado());
        }
    }
}