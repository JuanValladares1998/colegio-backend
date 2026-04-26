package com.cursoonline.exception.academico;

public class ModuloYaExisteException extends RuntimeException {
    public ModuloYaExisteException(String nombre) {
        super("Ya existe un módulo con el nombre '" + nombre + "' en este curso.");
    }
}