package com.cursoonline.exception.academico;

public class AnioEscolarNoEncontradoException extends RuntimeException {
    public AnioEscolarNoEncontradoException(Integer id) {
        super("Año escolar con ID " + id + " no encontrado.");
    }
}