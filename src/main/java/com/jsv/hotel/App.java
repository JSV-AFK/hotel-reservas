package com.jsv.hotel;

import com.jsv.hotel.model.HabitacionDoble;
import com.jsv.hotel.model.HabitacionSencilla;
import com.jsv.hotel.model.Suite;
import com.jsv.hotel.pricing.CalculadoraPrecio;
import com.jsv.hotel.pricing.DescuentoEstanciaLarga;
import com.jsv.hotel.pricing.DescuentoTemporadaBaja;
import com.jsv.hotel.pricing.PoliticaCancelacion;
import com.jsv.hotel.repository.HabitacionRepository;
import com.jsv.hotel.repository.sqlite.BaseDeDatos;
import com.jsv.hotel.repository.sqlite.HabitacionRepositorySqlite;
import com.jsv.hotel.repository.sqlite.HuespedRepositorySqlite;
import com.jsv.hotel.repository.sqlite.ReservaRepositorySqlite;
import com.jsv.hotel.service.ReservaService;
import com.jsv.hotel.ui.Consola;
import com.jsv.hotel.ui.MenuPrincipal;
import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;

public class App {

    public static void main(String[] args) {
        BaseDeDatos bd = new BaseDeDatos("hotel.db");
        HabitacionRepository habitaciones = new HabitacionRepositorySqlite(bd);
        cargarHabitacionesIniciales(habitaciones);

        ReservaService servicio = new ReservaService(
                habitaciones,
                new HuespedRepositorySqlite(bd),
                new ReservaRepositorySqlite(bd),
                new CalculadoraPrecio(List.of(new DescuentoEstanciaLarga(), new DescuentoTemporadaBaja())),
                new PoliticaCancelacion(),
                Clock.systemDefaultZone());

        new MenuPrincipal(new Consola(System.in, System.out), servicio).ejecutar();
    }

    /** Si la base está vacía, carga las habitaciones del hotel. */
    private static void cargarHabitacionesIniciales(HabitacionRepository habitaciones) {
        if (!habitaciones.listar().isEmpty()) {
            return;
        }
        habitaciones.guardar(new HabitacionSencilla(101, BigDecimal.valueOf(100_000)));
        habitaciones.guardar(new HabitacionSencilla(102, BigDecimal.valueOf(110_000)));
        habitaciones.guardar(new HabitacionDoble(201, BigDecimal.valueOf(150_000), false));
        habitaciones.guardar(new HabitacionDoble(202, BigDecimal.valueOf(150_000), true));
        habitaciones.guardar(new Suite(301, BigDecimal.valueOf(300_000)));
        habitaciones.guardar(new Suite(302, BigDecimal.valueOf(350_000)));
    }
}