package com.jsv.hotel.repository.memoria;

import com.jsv.hotel.model.Habitacion;
import com.jsv.hotel.repository.HabitacionRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class HabitacionRepositoryMemoria implements HabitacionRepository {

    private final Map<Integer, Habitacion> datos = new TreeMap<>();

    @Override
    public void guardar(Habitacion habitacion) {
        datos.put(habitacion.getNumero(), habitacion);
    }

    @Override
    public Optional<Habitacion> buscarPorNumero(int numero) {
        return Optional.ofNullable(datos.get(numero));
    }

    @Override
    public List<Habitacion> listar() {
        return List.copyOf(datos.values());
    }
}