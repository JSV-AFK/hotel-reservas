package com.jsv.hotel.pricing;

import com.jsv.hotel.model.Reserva;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class CalculadoraPrecio {

    private final List<Descuento> descuentos;

    public CalculadoraPrecio(List<Descuento> descuentos) {
        this.descuentos = List.copyOf(descuentos);
    }

    public Cotizacion cotizar(Reserva reserva) {
        BigDecimal subtotal = reserva.calcularSubtotal();

        Descuento elegido = null;
        BigDecimal mayorPorcentaje = BigDecimal.ZERO;
        for (Descuento descuento : descuentos) {
            BigDecimal porcentaje = descuento.porcentaje(reserva);
            if (porcentaje.compareTo(mayorPorcentaje) > 0) {
                elegido = descuento;
                mayorPorcentaje = porcentaje;
            }
        }

        BigDecimal valorDescuento = subtotal.multiply(mayorPorcentaje)
                .divide(BigDecimal.valueOf(100), 0, RoundingMode.HALF_UP);

        return new Cotizacion(
                subtotal,
                elegido == null ? "Sin descuento" : elegido.nombre(),
                mayorPorcentaje,
                valorDescuento,
                subtotal.subtract(valorDescuento));
    }
}