package com.cursoonline.exception.academico;

public class ModuloNoEncontradoException extends RuntimeException {
    public ModuloNoEncontradoException(Integer id) {
        super("Módulo con ID " + id + " no encontrado.");
    }
}