package com.jsv.hotel.model;

public class Huesped {

    private Long id;
    private final String nombre;
    private final String documento;
    private final String correo;
    private final String telefono;

    public Huesped(String nombre, String documento, String correo, String telefono) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }
        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }
        if (correo == null || !correo.contains("@")) {
            throw new IllegalArgumentException("El correo no es válido");
        }
        this.nombre = nombre.strip();
        this.documento = documento.strip();
        this.correo = correo.strip().toLowerCase();
        this.telefono = telefono == null ? "" : telefono.strip();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDocumento() {
        return documento;
    }

    public String getCorreo() {
        return correo;
    }

    public String getTelefono() {
        return telefono;
    }

    @Override
    public String toString() {
        return "%s (%s)".formatted(nombre, documento);
    }
}