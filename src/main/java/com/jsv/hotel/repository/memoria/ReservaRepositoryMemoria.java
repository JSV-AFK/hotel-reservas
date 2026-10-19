package com.jsv.hotel.repository.memoria;

import com.jsv.hotel.model.Reserva;
import com.jsv.hotel.repository.ReservaRepository;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class ReservaRepositoryMemoria implements ReservaRepository {

    private final Map<Long, Reserva> datos = new LinkedHashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public Reserva guardar(Reserva reserva) {
        if (reserva.getId() == null) {
            reserva.setId(secuencia.incrementAndGet());
        }
        datos.put(reserva.getId(), reserva);
        return reserva;
    }

    @Override
    public Optional<Reserva> buscarPorId(long id) {
        return Optional.ofNullable(datos.get(id));
    }

    @Override
    public List<Reserva> listarPorHabitacion(int numeroHabitacion) {
        return datos.values().stream()
                .filter(r -> r.getHabitacion().getNumero() == numeroHabitacion)
                .toList();
    }

    @Override
    public List<Reserva> listar() {
        return List.copyOf(datos.values());
    }
}