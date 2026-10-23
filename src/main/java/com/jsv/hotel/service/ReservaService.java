package com.jsv.hotel.service;

import com.jsv.hotel.exception.HabitacionNoDisponibleException;
import com.jsv.hotel.exception.RecursoNoEncontradoException;
import com.jsv.hotel.exception.ReservaInvalidaException;
import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.model.Reserva;
import com.jsv.hotel.pricing.CalculadoraPrecio;
import com.jsv.hotel.pricing.Cotizacion;
import com.jsv.hotel.pricing.PoliticaCancelacion;
import com.jsv.hotel.repository.HabitacionRepository;
import com.jsv.hotel.repository.HuespedRepository;
import com.jsv.hotel.repository.ReservaRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

public class ReservaService {

    private final HabitacionRepository habitaciones;
    private final HuespedRepository huespedes;
    private final ReservaRepository reservas;
    private final CalculadoraPrecio calculadora;
    private final PoliticaCancelacion politica;
    private final Clock reloj;

    public ReservaService(HabitacionRepository habitaciones, HuespedRepository huespedes,
                          ReservaRepository reservas, CalculadoraPrecio calculadora,
                          PoliticaCancelacion politica, Clock reloj) {
        this.habitaciones = habitaciones;
        this.huespedes = huespedes;
        this.reservas = reservas;
        this.calculadora = calculadora;
        this.politica = politica;
        this.reloj = reloj;
    }

    public List<Habitacion> buscarDisponibles(LocalDate entrada, LocalDate salida, int numeroHuespedes) {
        validarFechas(entrada, salida);
        return habitaciones.listar().stream()
                .filter(h -> h.admite(numeroHuespedes))
                .filter(h -> estaDisponible(h, entrada, salida))
                .toList();
    }

    public Reserva reservar(int numeroHabitacion, Huesped huesped, LocalDate entrada,
                            LocalDate salida, int numeroHuespedes) {
        validarFechas(entrada, salida);

        Habitacion habitacion = habitaciones.buscarPorNumero(numeroHabitacion)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la habitación " + numeroHabitacion));

        // Si el documento ya está registrado, se reutiliza ese huésped.
        Huesped titular = huespedes.buscarPorDocumento(huesped.getDocumento()).orElse(huesped);

        Reserva reserva = new Reserva(habitacion, titular, entrada, salida, numeroHuespedes);

        if (!estaDisponible(habitacion, entrada, salida)) {
            throw new HabitacionNoDisponibleException(
                    "La habitación %d no está disponible entre %s y %s"
                            .formatted(numeroHabitacion, entrada, salida));
        }

        if (titular.getId() == null) {
            huespedes.guardar(titular);
        }
        return reservas.guardar(reserva);
    }

    public Cotizacion cotizar(Reserva reserva) {
        return calculadora.cotizar(reserva);
    }

    public Cancelacion cancelar(long reservaId) {
        Reserva reserva = obtener(reservaId);
        reserva.cancelar(); // falla si la reserva no está confirmada

        BigDecimal total = calculadora.cotizar(reserva).total();
        BigDecimal penalizacion = politica.penalizacion(reserva, total, hoy());

        reservas.guardar(reserva);
        return new Cancelacion(reserva, penalizacion);
    }

    public Reserva hacerCheckIn(long reservaId) {
        Reserva reserva = obtener(reservaId);
        if (hoy().isBefore(reserva.getCheckIn())) {
            throw new ReservaInvalidaException(
                    "El check-in solo puede hacerse desde la fecha de entrada (" + reserva.getCheckIn() + ")");
        }
        reserva.hacerCheckIn();
        return reservas.guardar(reserva);
    }

    public Reserva hacerCheckOut(long reservaId) {
        Reserva reserva = obtener(reservaId);
        reserva.hacerCheckOut();
        return reservas.guardar(reserva);
    }

    public Reserva obtener(long reservaId) {
        return reservas.buscarPorId(reservaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la reserva " + reservaId));
    }
    public List<Habitacion> listarHabitaciones() {
        return habitaciones.listar();
    }

    public List<Reserva> listarReservas() {
        return reservas.listar();
    }
    
    private boolean estaDisponible(Habitacion habitacion, LocalDate entrada, LocalDate salida) {
        return reservas.listarPorHabitacion(habitacion.getNumero()).stream()
                .filter(r -> r.getEstado().ocupaHabitacion())
                .noneMatch(r -> r.solapaCon(entrada, salida));
    }

    private void validarFechas(LocalDate entrada, LocalDate salida) {
        if (entrada == null || salida == null) {
            throw new ReservaInvalidaException("Las fechas son obligatorias");
        }
        if (!salida.isAfter(entrada)) {
            throw new ReservaInvalidaException("La salida debe ser posterior a la entrada");
        }
        if (entrada.isBefore(hoy())) {
            throw new ReservaInvalidaException("La fecha de entrada no puede estar en el pasado");
        }
    }

    private LocalDate hoy() {
        return LocalDate.now(reloj);
    }
}