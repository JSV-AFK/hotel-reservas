package com.jsv.hotel.model;

import com.jsv.hotel.exception.EstadoReservaInvalidoException;
import com.jsv.hotel.exception.ReservaInvalidaException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Reserva {

    private Long id;
    private final Habitacion habitacion;
    private final Huesped huesped;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final int numeroHuespedes;
    private EstadoReserva estado = EstadoReserva.CONFIRMADA;

    public Reserva(Habitacion habitacion, Huesped huesped, LocalDate checkIn,
                   LocalDate checkOut, int numeroHuespedes) {
        Objects.requireNonNull(habitacion, "La habitación es obligatoria");
        Objects.requireNonNull(huesped, "El huésped es obligatorio");
        Objects.requireNonNull(checkIn, "La fecha de entrada es obligatoria");
        Objects.requireNonNull(checkOut, "La fecha de salida es obligatoria");

        if (!checkOut.isAfter(checkIn)) {
            throw new ReservaInvalidaException("La salida debe ser posterior a la entrada");
        }
        if (!habitacion.admite(numeroHuespedes)) {
            throw new ReservaInvalidaException(
                    "La habitación %d admite máximo %d huésped(es)"
                            .formatted(habitacion.getNumero(), habitacion.getCapacidad()));
        }

        this.habitacion = habitacion;
        this.huesped = huesped;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.numeroHuespedes = numeroHuespedes;
    }

    public long getNoches() {
        return ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public BigDecimal calcularSubtotal() {
        return habitacion.calcularPrecioPorNoche().multiply(BigDecimal.valueOf(getNoches()));
    }

 
    public boolean solapaCon(LocalDate otraEntrada, LocalDate otraSalida) {
        return checkIn.isBefore(otraSalida) && otraEntrada.isBefore(checkOut);
    }

    public void hacerCheckIn() {
        exigirEstado(EstadoReserva.CONFIRMADA, "hacer check-in");
        estado = EstadoReserva.EN_CURSO;
    }

    public void hacerCheckOut() {
        exigirEstado(EstadoReserva.EN_CURSO, "hacer check-out");
        estado = EstadoReserva.FINALIZADA;
    }

    public void cancelar() {
        exigirEstado(EstadoReserva.CONFIRMADA, "cancelar");
        estado = EstadoReserva.CANCELADA;
    }

    private void exigirEstado(EstadoReserva requerido, String accion) {
        if (estado != requerido) {
            throw new EstadoReservaInvalidoException(
                    "No se puede %s una reserva en estado %s".formatted(accion, estado));
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Habitacion getHabitacion() {
        return habitacion;
    }

    public Huesped getHuesped() {
        return huesped;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public int getNumeroHuespedes() {
        return numeroHuespedes;
    }

    public EstadoReserva getEstado() {
        return estado;
    }
}