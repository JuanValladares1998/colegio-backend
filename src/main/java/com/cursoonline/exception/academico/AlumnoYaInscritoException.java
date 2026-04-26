package com.cursoonline.exception.academico;

public class AlumnoYaInscritoException extends RuntimeException {

    public AlumnoYaInscritoException(String mensaje) {
        super(mensaje);
    }

    public static AlumnoYaInscritoException enMismaSeccion() {
        return new AlumnoYaInscritoException(
                "El alumno ya está inscrito activamente en esta sección.");
    }

    public static AlumnoYaInscritoException enOtraSeccionDelAnio() {
        return new AlumnoYaInscritoException(
                "El alumno ya está inscrito en otra sección del mismo año escolar.");
    }
}