package com.cursoonline.exception.academico;

public class LeccionYaExisteException extends RuntimeException {
    public LeccionYaExisteException(String nombre) {
        super("Ya existe una lección con el nombre '" + nombre + "' en este módulo.");
    }
}