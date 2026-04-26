package com.cursoonline.exception.academico;

public class SeccionNoEncontradaException extends RuntimeException {
    public SeccionNoEncontradaException(Integer id) {
        super("Sección con ID " + id + " no encontrada.");
    }
}