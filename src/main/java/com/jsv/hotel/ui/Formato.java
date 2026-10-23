package com.jsv.hotel.ui;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Locale;

/** Formatos de fechas y dinero para mostrar al usuario. */
public final class Formato {

    /** dd/mm/aaaa, estricto: rechaza fechas inexistentes como 31/02/2026. */
    public static final DateTimeFormatter FECHA =
            DateTimeFormatter.ofPattern("dd/MM/uuuu").withResolverStyle(ResolverStyle.STRICT);

    private Formato() {
    }

    public static String fecha(LocalDate fecha) {
        return FECHA.format(fecha);
    }

    /** Pesos con punto como separador de miles, sin depender del idioma del sistema. */
    public static String dinero(BigDecimal valor) {
        DecimalFormatSymbols simbolos = new DecimalFormatSymbols(Locale.ROOT);
        simbolos.setGroupingSeparator('.');
        simbolos.setDecimalSeparator(',');
        return "$" + new DecimalFormat("#,##0", simbolos).format(valor);
    }
}