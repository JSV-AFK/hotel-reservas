package com.jsv.hotel.repository.memoria;

import com.jsv.hotel.model.Huesped;
import com.jsv.hotel.repository.HuespedRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class HuespedRepositoryMemoria implements HuespedRepository {

    private final Map<String, Huesped> porDocumento = new HashMap<>();
    private final AtomicLong secuencia = new AtomicLong();

    @Override
    public Huesped guardar(Huesped huesped) {
        if (huesped.getId() == null) {
            huesped.setId(secuencia.incrementAndGet());
        }
        porDocumento.put(huesped.getDocumento(), huesped);
        return huesped;
    }

    @Override
    public Optional<Huesped> buscarPorDocumento(String documento) {
        return Optional.ofNullable(porDocumento.get(documento));
    }
}