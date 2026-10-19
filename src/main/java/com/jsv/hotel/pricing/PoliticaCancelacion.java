package com.jsv.hotel.pricing;

import com.jsv.hotel.model.Reserva;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class PoliticaCancelacion {

    /**
     * 7 días o más de anticipación: sin costo.
     * Entre 2 y 6 días: el valor de la primera noche.
     * Menos de 2 días: el 50 % del total.
     * La penalización nunca supera el total de la reserva.
     */
    public BigDecimal penalizacion(Reserva reserva, BigDecimal total, LocalDate hoy) {
        long dias = ChronoUnit.DAYS.between(hoy, reserva.getCheckIn());

        if (dias >= 7) {
            return BigDecimal.ZERO;
        }
        if (dias >= 2) {
            return reserva.getHabitacion().calcularPrecioPorNoche().min(total);
        }
        return total.divide(BigDecimal.valueOf(2), 0, RoundingMode.HALF_UP);
    }
}