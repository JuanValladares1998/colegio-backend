package com.cursoonline.exception.academico;

public class NivelNoEncontradoException extends RuntimeException {
    public NivelNoEncontradoException(Integer id) {
        super("Nivel con ID " + id + " no encontrado.");
    }
}