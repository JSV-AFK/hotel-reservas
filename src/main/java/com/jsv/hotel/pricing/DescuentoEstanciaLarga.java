package com.jsv.hotel.pricing;

import com.jsv.hotel.model.Reserva;
import java.math.BigDecimal;

public class DescuentoEstanciaLarga implements Descuento {

    @Override
    public String nombre() {
        return "Estancia larga";
    }

    @Override
    public BigDecimal porcentaje(Reserva reserva) {
        long noches = reserva.getNoches();
        if (noches >= 14) {
            return BigDecimal.valueOf(15);
        }
        if (noches >= 7) {
            return BigDecimal.valueOf(10);
        }
        return BigDecimal.ZERO;
    }
}