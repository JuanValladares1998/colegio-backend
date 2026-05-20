package com.cursoonline.exception.academico;

public class AlumnoNoMatriculadoException extends RuntimeException {
    public AlumnoNoMatriculadoException() {
        super("El alumno seleccionado no se encuentra matriculado en este curso.");
    }
}