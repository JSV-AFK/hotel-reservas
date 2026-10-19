package com.jsv.hotel.repository;

import com.jsv.hotel.model.Habitacion;
import java.util.List;
import java.util.Optional;

public interface HabitacionRepository {

    void guardar(Habitacion habitacion);

    Optional<Habitacion> buscarPorNumero(int numero);

    /** Devuelve las habitaciones ordenadas por número. */
    List<Habitacion> listar();
}