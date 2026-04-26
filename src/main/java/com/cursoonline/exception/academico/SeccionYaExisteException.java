package com.cursoonline.exception.academico;

public class SeccionYaExisteException extends RuntimeException {
    public SeccionYaExisteException(String nombre) {
        super("Ya existe una sección con el nombre '" + nombre + "' en ese curso y año escolar.");
    }
}