package com.cursoonline.exception.academico;

public class ProfesorYaAsignadoException extends RuntimeException {
    public ProfesorYaAsignadoException() {
        super("Esta sección ya tiene un profesor asignado.");
    }
}