package com.jsv.hotel.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class HabitacionDoble extends Habitacion {

    private static final BigDecimal RECARGO_BALCON = new BigDecimal("1.10");

    private final boolean conBalcon;

    public HabitacionDoble(int numero, BigDecimal precioBase, boolean conBalcon) {
        super(numero, precioBase);
        this.conBalcon = conBalcon;
    }

    public boolean tieneBalcon() {
        return conBalcon;
    }

    @Override
    public String getTipo() {
        return conBalcon ? "Doble con balcón" : "Doble";
    }

    @Override
    public int getCapacidad() {
        return 2;
    }

    @Override
    public BigDecimal calcularPrecioPorNoche() {
        if (!conBalcon) {
            return getPrecioBase();
        }
        return getPrecioBase().multiply(RECARGO_BALCON).setScale(0, RoundingMode.HALF_UP);
    }
}
