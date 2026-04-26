package com.cursoonline.exception.academico;

public class CursoYaExisteException extends RuntimeException {
    public CursoYaExisteException(String nombre) {
        super("Ya existe un curso con el nombre '" + nombre + "'.");
    }
}