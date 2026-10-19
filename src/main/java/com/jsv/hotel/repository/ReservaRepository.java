package com.jsv.hotel.repository;

import com.jsv.hotel.model.Reserva;
import java.util.List;
import java.util.Optional;

public interface ReservaRepository {

    Reserva guardar(Reserva reserva);

    Optional<Reserva> buscarPorId(long id);

    List<Reserva> listarPorHabitacion(int numeroHabitacion);

    List<Reserva> listar();
}