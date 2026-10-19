package com.jsv.hotel.pricing;

import java.math.BigDecimal;

public record Cotizacion(
        BigDecimal subtotal,
        String descuento,
        BigDecimal porcentaje,
        BigDecimal valorDescuento,
        BigDecimal total) {
}