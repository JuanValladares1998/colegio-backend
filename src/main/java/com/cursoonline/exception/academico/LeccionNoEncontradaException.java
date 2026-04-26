package com.cursoonline.exception.academico;

public class LeccionNoEncontradaException extends RuntimeException {
    public LeccionNoEncontradaException(Integer id) {
        super("Lección con ID " + id + " no encontrada.");
    }
}