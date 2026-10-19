package com.jsv.hotel.model;

import java.math.BigDecimal;

public class HabitacionSencilla extends Habitacion {

    public HabitacionSencilla(int numero, BigDecimal precioBase) {
        super(numero, precioBase);
    }

    @Override
    public String getTipo() {
        return "Sencilla";
    }

    @Override
    public int getCapacidad() {
        return 1;
    }

    @Override
    public BigDecimal calcularPrecioPorNoche() {
        return getPrecioBase();
    }
}