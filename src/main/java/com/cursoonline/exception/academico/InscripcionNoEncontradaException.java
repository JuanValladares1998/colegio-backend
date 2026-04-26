package com.cursoonline.exception.academico;

public class InscripcionNoEncontradaException extends RuntimeException {
    public InscripcionNoEncontradaException(Integer idAlumno, Integer idSeccion) {
        super("No existe una inscripción activa del alumno " + idAlumno +
              " en la sección " + idSeccion + ".");
    }
}