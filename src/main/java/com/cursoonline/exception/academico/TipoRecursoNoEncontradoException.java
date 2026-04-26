package com.cursoonline.exception.academico;

public class TipoRecursoNoEncontradoException extends RuntimeException {
    public TipoRecursoNoEncontradoException(Integer id) {
        super("Tipo de recurso con ID " + id + " no encontrado.");
    }
}