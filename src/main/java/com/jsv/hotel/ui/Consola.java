package com.jsv.hotel.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/** Lee y escribe en la consola. Vuelve a preguntar mientras el dato no sea válido. */
public class Consola {

    /** Se lanza cuando ya no hay más datos de entrada (por ejemplo, al cerrar la entrada con Ctrl+Z). */
    public static class EntradaTerminadaException extends RuntimeException {
        public EntradaTerminadaException() {
            super("Fin de la entrada de datos");
        }
    }

    private final BufferedReader entrada;
    private final PrintStream salida;

    public Consola(InputStream entrada, PrintStream salida) {
        this.entrada = new BufferedReader(new InputStreamReader(entrada, StandardCharsets.UTF_8));
        this.salida = salida;
    }

    public void imprimir(String texto) {
        salida.println(texto);
    }

    /** Texto obligatorio: insiste hasta que el usuario escriba algo. */
    public String leerTexto(String pregunta) {
        while (true) {
            String valor = leerOpcional(pregunta);
            if (!valor.isEmpty()) {
                return valor;
            }
            imprimir("  Este dato es obligatorio.");
        }
    }

    /** Texto que se puede dejar vacío. */
    public String leerOpcional(String pregunta) {
        salida.print(pregunta + ": ");
        salida.flush();
        return leerLinea().strip();
    }

    public int leerEntero(String pregunta, int minimo, int maximo) {
        while (true) {
            String texto = leerOpcional(pregunta);
            try {
                int valor = Integer.parseInt(texto);
                if (valor >= minimo && valor <= maximo) {
                    return valor;
                }
            } catch (NumberFormatException e) {
                // No era un número: se vuelve a preguntar.
            }
            imprimir("  Debes escribir un número entre %d y %d.".formatted(minimo, maximo));
        }
    }

    public LocalDate leerFecha(String pregunta) {
        while (true) {
            String texto = leerOpcional(pregunta + " (dd/mm/aaaa)");
            try {
                return LocalDate.parse(texto, Formato.FECHA);
            } catch (DateTimeParseException e) {
                imprimir("  Fecha no válida. Usa el formato dd/mm/aaaa, por ejemplo 25/12/2026.");
            }
        }
    }

    public boolean confirmar(String pregunta) {
        while (true) {
            String respuesta = leerOpcional(pregunta + " (s/n)").toLowerCase();
            if (respuesta.equals("s") || respuesta.equals("si") || respuesta.equals("sí")) {
                return true;
            }
            if (respuesta.equals("n") || respuesta.equals("no")) {
                return false;
            }
            imprimir("  Responde s o n.");
        }
    }

    private String leerLinea() {
        try {
            String linea = entrada.readLine();
            if (linea == null) {
                throw new EntradaTerminadaException();
            }
            return linea;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}