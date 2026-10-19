package com.jsv.hotel.pricing;

import com.jsv.hotel.model.Reserva;
import java.math.BigDecimal;

public interface Descuento {

    String nombre();

    BigDecimal porcentaje(Reserva reserva);
}