package com.jsv.hotel.exception;

/** Error técnico al acceder a la base de datos (no es una regla de negocio). */
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}