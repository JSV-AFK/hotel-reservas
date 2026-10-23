package com.jsv.hotel.ui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jsv.hotel.model.HabitacionDoble;
import com.jsv.hotel.model.HabitacionSencilla;
import com.jsv.hotel.model.Suite;
import com.jsv.hotel.pricing.CalculadoraPrecio;
import com.jsv.hotel.pricing.DescuentoEstanciaLarga;
import com.jsv.hotel.pricing.DescuentoTemporadaBaja;
import com.jsv.hotel.pricing.PoliticaCancelacion;
import com.jsv.hotel.repository.HabitacionRepository;
import com.jsv.hotel.repository.memoria.HabitacionRepositoryMemoria;
import com.jsv.hotel.repository.memoria.HuespedRepositoryMemoria;
import com.jsv.hotel.repository.memoria.ReservaRepositoryMemoria;
import com.jsv.hotel.service.ReservaService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MenuPrincipalTest {

    private ReservaService servicio;

    @BeforeEach
    void preparar() {
        HabitacionRepository habitaciones = new HabitacionRepositoryMemoria();
        habitaciones.guardar(new HabitacionSencilla(101, BigDecimal.valueOf(100_000)));
        habitaciones.guardar(new HabitacionDoble(201, BigDecimal.valueOf(150_000), false));
        habitaciones.guardar(new Suite(301, BigDecimal.valueOf(300_000)));

        // "Hoy" es siempre el 19 de octubre de 2026.
        Clock reloj = Clock.fixed(Instant.parse("2026-10-19T12:00:00Z"), ZoneId.of("America/Bogota"));
        servicio = new ReservaService(
                habitaciones,
                new HuespedRepositoryMemoria(),
                new ReservaRepositoryMemoria(),
                new CalculadoraPrecio(List.of(new DescuentoEstanciaLarga(), new DescuentoTemporadaBaja())),
                new PoliticaCancelacion(),
                reloj);
    }

    /** Simula a un usuario que escribe una línea por cada elemento y devuelve todo lo que se imprimió. */
    private String ejecutar(List<String> lineas) {
        String entrada = String.join("\n", lineas) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream salida = new PrintStream(buffer, true, StandardCharsets.UTF_8);
        Consola consola = new Consola(
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)), salida);

        new MenuPrincipal(consola, servicio).ejecutar();

        return buffer.toString(StandardCharsets.UTF_8);
    }

    /** Opción 3 completa: la habitación 101 para Ana, del 10 al 12 de diciembre. */
    private static List<String> crearReservaDeAna() {
        return List.of("3", "10/12/2026", "12/12/2026", "1", "101",
                "1020304050", "Ana Gómez", "3001234567", "ana@gmail.com", "s");
    }

    @Test
    void creaUnaReservaYLaMuestraEnElListado() {
        List<String> pasos = new ArrayList<>(crearReservaDeAna());
        pasos.addAll(List.of("4", "0"));

        String pantalla = ejecutar(pasos);

        assertTrue(pantalla.contains("Reserva #1 creada"));
        assertTrue(pantalla.contains("Ana Gómez"));
        assertTrue(pantalla.contains("[CONFIRMADA]"));
        assertTrue(pantalla.contains("$200.000")); // 2 noches a 100.000
    }

    @Test
    void cancelaUnaReservaSinPenalizacionConAnticipacion() {
        List<String> pasos = new ArrayList<>(crearReservaDeAna());
        pasos.addAll(List.of("7", "1", "s", "0"));

        String pantalla = ejecutar(pasos);

        assertTrue(pantalla.contains("cancelada"));
        assertTrue(pantalla.contains("Penalización: $0"));
    }

    @Test
    void rechazaUnaOpcionInvalidaYSigueFuncionando() {
        String pantalla = ejecutar(List.of("9", "0"));

        assertTrue(pantalla.contains("entre 0 y 7"));
        assertTrue(pantalla.contains("¡Hasta pronto!"));
    }

    @Test
    void vuelveAPedirUnaFechaInvalida() {
        String pantalla = ejecutar(List.of("2", "31/02/2026", "10/12/2026", "12/12/2026", "1", "0"));

        assertTrue(pantalla.contains("Fecha no válida"));
        assertTrue(pantalla.contains("#101"));
    }

    @Test
    void muestraElErrorDeNegocioSinCerrarElPrograma() {
        String pantalla = ejecutar(List.of("5", "99", "0"));

        assertTrue(pantalla.contains("No existe la reserva 99"));
        assertTrue(pantalla.contains("¡Hasta pronto!"));
    }

    @Test
    void avisaCuandoNoHayReservas() {
        String pantalla = ejecutar(List.of("4", "0"));

        assertTrue(pantalla.contains("No hay reservas registradas"));
    }

    @Test
    void terminaSinErrorCuandoSeCierraLaEntrada() {
        String pantalla = ejecutar(List.of());

        assertTrue(pantalla.contains("¡Hasta pronto!"));
    }
}