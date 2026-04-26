package com.cursoonline.exception.academico;

public class CursoNoEncontradoException extends RuntimeException {
    public CursoNoEncontradoException(Integer id) {
        super("Curso con ID " + id + " no encontrado.");
    }
}