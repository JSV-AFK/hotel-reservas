package com.jsv.hotel.model;

public enum EstadoReserva {
    CONFIRMADA,
    EN_CURSO,
    FINALIZADA,
    CANCELADA;

    public boolean ocupaHabitacion() {
        return this == CONFIRMADA || this == EN_CURSO;
    }
}