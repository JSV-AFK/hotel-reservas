package com.jsv.hotel.pricing;

import com.jsv.hotel.model.Reserva;
import java.math.BigDecimal;
import java.time.Month;
import java.util.Set;

public class DescuentoTemporadaBaja implements Descuento {

    private static final Set<Month> MESES_BAJOS =
            Set.of(Month.FEBRUARY, Month.MARCH, Month.SEPTEMBER, Month.OCTOBER);

    @Override
    public String nombre() {
        return "Temporada baja";
    }

    @Override
    public BigDecimal porcentaje(Reserva reserva) {
        return MESES_BAJOS.contains(reserva.getCheckIn().getMonth())
                ? BigDecimal.valueOf(10)
                : BigDecimal.ZERO;
    }
}