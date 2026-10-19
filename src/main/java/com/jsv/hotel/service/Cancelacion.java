package com.jsv.hotel.service;

import com.jsv.hotel.model.Reserva;
import java.math.BigDecimal;

public record Cancelacion(Reserva reserva, BigDecimal penalizacion) {
}