package com.jsv.hotel.ui;

import com.jsv.hotel.exception.HotelException;
import com.jsv.hotel.exception.PersistenciaException;
import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.model.Reserva;
import com.jsv.hotel.pricing.Cotizacion;
import com.jsv.hotel.service.Cancelacion;
import com.jsv.hotel.service.ReservaService;
import java.time.LocalDate;
import java.util.List;

/** Menú interactivo. Pide datos, llama al servicio y muestra los resultados. */
public class MenuPrincipal {

    private static final int MAX_HUESPEDES = 10;

    private final Consola consola;
    private final ReservaService servicio;

    public MenuPrincipal(Consola consola, ReservaService servicio) {
        this.consola = consola;
        this.servicio = servicio;
    }

    public void ejecutar() {
        consola.imprimir("=== Sistema de Reservas de Hotel ===");
        try {
            boolean continuar = true;
            while (continuar) {
                mostrarMenu();
                continuar = procesar(consola.leerEntero("Elige una opción", 0, 7));
            }
        } catch (Consola.EntradaTerminadaException e) {
            // La entrada se cerró: el programa termina sin error.
        }
        consola.imprimir("");
        consola.imprimir("¡Hasta pronto!");
    }

    private void mostrarMenu() {
        consola.imprimir("");
        consola.imprimir("1. Ver habitaciones");
        consola.imprimir("2. Buscar habitaciones disponibles");
        consola.imprimir("3. Crear una reserva");
        consola.imprimir("4. Ver reservas");
        consola.imprimir("5. Hacer check-in");
        consola.imprimir("6. Hacer check-out");
        consola.imprimir("7. Cancelar una reserva");
        consola.imprimir("0. Salir");
    }

    /** Ejecuta la opción elegida. Devuelve false cuando el usuario quiere salir. */
    private boolean procesar(int opcion) {
        if (opcion == 0) {
            return false;
        }
        try {
            switch (opcion) {
                case 1 -> verHabitaciones();
                case 2 -> buscarDisponibles();
                case 3 -> crearReserva();
                case 4 -> verReservas();
                case 5 -> hacerCheckIn();
                case 6 -> hacerCheckOut();
                case 7 -> cancelarReserva();
                default -> { }
            }
        } catch (HotelException | IllegalArgumentException e) {
            consola.imprimir("  Error: " + e.getMessage());
        } catch (PersistenciaException e) {
            consola.imprimir("  Error técnico al acceder a la base de datos: " + e.getMessage());
        }
        return true;
    }

    // ---------- Habitaciones ----------

    private void verHabitaciones() {
        consola.imprimir("");
        consola.imprimir("--- Habitaciones ---");
        servicio.listarHabitaciones().forEach(h -> consola.imprimir(descripcion(h)));
    }

    private void buscarDisponibles() {
        LocalDate entrada = consola.leerFecha("Fecha de entrada");
        LocalDate salida = consola.leerFecha("Fecha de salida");
        int huespedes = consola.leerEntero("Número de huéspedes", 1, MAX_HUESPEDES);

        mostrarDisponibles(servicio.buscarDisponibles(entrada, salida, huespedes));
    }

    private void mostrarDisponibles(List<Habitacion> disponibles) {
        consola.imprimir("");
        if (disponibles.isEmpty()) {
            consola.imprimir("No hay habitaciones disponibles para esos datos.");
            return;
        }
        consola.imprimir("--- Habitaciones disponibles ---");
        disponibles.forEach(h -> consola.imprimir(descripcion(h)));
    }

    private static String descripcion(Habitacion h) {
        return "#%d  %-18s capacidad %d  %s por noche".formatted(
                h.getNumero(), h.getTipo(), h.getCapacidad(),
                Formato.dinero(h.calcularPrecioPorNoche()));
    }

    // ---------- Reservas ----------

    private void crearReserva() {
        LocalDate entrada = consola.leerFecha("Fecha de entrada");
        LocalDate salida = consola.leerFecha("Fecha de salida");
        int numeroHuespedes = consola.leerEntero("Número de huéspedes", 1, MAX_HUESPEDES);

        List<Habitacion> disponibles = servicio.buscarDisponibles(entrada, salida, numeroHuespedes);
        mostrarDisponibles(disponibles);
        if (disponibles.isEmpty()) {
            return;
        }

        Habitacion elegida = elegirHabitacion(disponibles);
        Huesped huesped = pedirHuesped();

        // Se arma un borrador solo para mostrar el precio; todavía no se guarda nada.
        Reserva borrador = new Reserva(elegida, huesped, entrada, salida, numeroHuespedes);
        mostrarResumen(borrador, servicio.cotizar(borrador));

        if (!consola.confirmar("¿Confirmar la reserva?")) {
            consola.imprimir("Reserva descartada.");
            return;
        }
        Reserva reserva = servicio.reservar(elegida.getNumero(), huesped, entrada, salida, numeroHuespedes);
        consola.imprimir("Reserva #%d creada.".formatted(reserva.getId()));
    }

    private Habitacion elegirHabitacion(List<Habitacion> disponibles) {
        while (true) {
            int numero = consola.leerEntero("Número de la habitación que quieres", 1, 9999);
            for (Habitacion habitacion : disponibles) {
                if (habitacion.getNumero() == numero) {
                    return habitacion;
                }
            }
            consola.imprimir("  Esa habitación no está en la lista de disponibles.");
        }
    }

    private Huesped pedirHuesped() {
        consola.imprimir("");
        consola.imprimir("--- Datos del huésped ---");
        String documento = consola.leerTexto("Documento");
        String nombre = consola.leerTexto("Nombre completo");
        String telefono = consola.leerOpcional("Teléfono (opcional)");
        while (true) {
            String correo = consola.leerTexto("Correo electrónico");
            try {
                return new Huesped(nombre, documento, correo, telefono);
            } catch (IllegalArgumentException e) {
                consola.imprimir("  " + e.getMessage());
            }
        }
    }

    private void mostrarResumen(Reserva reserva, Cotizacion cotizacion) {
        consola.imprimir("");
        consola.imprimir("--- Resumen ---");
        consola.imprimir("Habitación: " + reserva.getHabitacion());
        consola.imprimir("Huésped:    " + reserva.getHuesped());
        consola.imprimir("Fechas:     %s a %s (%d noches)".formatted(
                Formato.fecha(reserva.getCheckIn()), Formato.fecha(reserva.getCheckOut()),
                reserva.getNoches()));
        consola.imprimir("Subtotal:   " + Formato.dinero(cotizacion.subtotal()));
        if (cotizacion.valorDescuento().signum() > 0) {
            consola.imprimir("Descuento:  %s (%s%%) -%s".formatted(
                    cotizacion.descuento(),
                    cotizacion.porcentaje().stripTrailingZeros().toPlainString(),
                    Formato.dinero(cotizacion.valorDescuento())));
        }
        consola.imprimir("Total:      " + Formato.dinero(cotizacion.total()));
    }

    private void verReservas() {
        List<Reserva> reservas = servicio.listarReservas();
        consola.imprimir("");
        if (reservas.isEmpty()) {
            consola.imprimir("No hay reservas registradas.");
            return;
        }
        consola.imprimir("--- Reservas ---");
        reservas.forEach(r -> consola.imprimir(lineaDeReserva(r)));
    }

    private String lineaDeReserva(Reserva r) {
        return "#%d  %s  %s  %s -> %s  %d noche(s)  %s  [%s]".formatted(
                r.getId(), r.getHabitacion(), r.getHuesped().getNombre(),
                Formato.fecha(r.getCheckIn()), Formato.fecha(r.getCheckOut()),
                r.getNoches(), Formato.dinero(servicio.cotizar(r).total()), r.getEstado());
    }

    private void hacerCheckIn() {
        Reserva reserva = servicio.hacerCheckIn(pedirIdReserva());
        consola.imprimir("Check-in realizado. Reserva #%d en estado %s."
                .formatted(reserva.getId(), reserva.getEstado()));
    }

    private void hacerCheckOut() {
        Reserva reserva = servicio.hacerCheckOut(pedirIdReserva());
        consola.imprimir("Check-out realizado. Reserva #%d en estado %s."
                .formatted(reserva.getId(), reserva.getEstado()));
    }

    private void cancelarReserva() {
        long id = pedirIdReserva();
        Reserva reserva = servicio.obtener(id);

        consola.imprimir("");
        consola.imprimir(lineaDeReserva(reserva));
        consola.imprimir("Política: sin costo con 7 días o más de anticipación; entre 2 y 6 días, "
                + "se cobra la primera noche; con menos de 2 días, el 50 % del total.");

        if (!consola.confirmar("¿Cancelar esta reserva?")) {
            consola.imprimir("No se canceló la reserva.");
            return;
        }
        Cancelacion cancelacion = servicio.cancelar(id);
        consola.imprimir("Reserva #%d cancelada. Penalización: %s"
                .formatted(id, Formato.dinero(cancelacion.penalizacion())));
    }

    private long pedirIdReserva() {
        return consola.leerEntero("Número de la reserva", 1, 999_999);
    }
}