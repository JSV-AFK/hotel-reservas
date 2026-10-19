package com.jsv.hotel.repository;

import com.jsv.hotel.model.Huesped;
import java.util.Optional;

public interface HuespedRepository {

    Huesped guardar(Huesped huesped);

    Optional<Huesped> buscarPorDocumento(String documento);
}