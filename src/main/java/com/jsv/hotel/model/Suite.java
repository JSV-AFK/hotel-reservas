package com.jsv.hotel.model;

import java.math.BigDecimal;

public class Suite extends Habitacion {

    private static final BigDecimal DESAYUNO_POR_NOCHE = BigDecimal.valueOf(40_000);

    public Suite(int numero, BigDecimal precioBase) {
        super(numero, precioBase);
    }

    @Override
    public String getTipo() {
        return "Suite";
    }

    @Override
    public int getCapacidad() {
        return 4;
    }

    @Override
    public BigDecimal calcularPrecioPorNoche() {
        return getPrecioBase().add(DESAYUNO_POR_NOCHE);
    }
}