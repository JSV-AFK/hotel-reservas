package com.jsv.hotel.model;

import java.math.BigDecimal;

public abstract class Habitacion {

    private final int numero;
    private final BigDecimal precioBase;

    protected Habitacion(int numero, BigDecimal precioBase) {
        if (numero <= 0) {
            throw new IllegalArgumentException("El número de habitación debe ser positivo");
        }
        if (precioBase == null || precioBase.signum() <= 0) {
            throw new IllegalArgumentException("El precio base debe ser mayor que cero");
        }
        this.numero = numero;
        this.precioBase = precioBase;
    }

    public abstract String getTipo();

    public abstract int getCapacidad();

    public abstract BigDecimal calcularPrecioPorNoche();

    public boolean admite(int huespedes) {
        return huespedes > 0 && huespedes <= getCapacidad();
    }

    public int getNumero() {
        return numero;
    }

    public BigDecimal getPrecioBase() {
        return precioBase;
    }

    @Override
    public boolean equals(Object otro) {
        return otro instanceof Habitacion h && h.numero == numero;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(numero);
    }

    @Override
    public String toString() {
        return "%s #%d".formatted(getTipo(), numero);
    }
}