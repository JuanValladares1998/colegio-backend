package com.cursoonline.exception.academico;

public class RecursoNoEncontradoException extends RuntimeException {
    public RecursoNoEncontradoException(Integer id) {
        super("Recurso con ID " + id + " no encontrado.");
    }
}