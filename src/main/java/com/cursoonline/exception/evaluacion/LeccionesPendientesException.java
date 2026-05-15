package com.cursoonline.exception.evaluacion;
public class LeccionesPendientesException extends RuntimeException {
    public LeccionesPendientesException(long cuantas) {
        super("Debes completar las " + cuantas + " lecciones obligatorias del módulo antes de rendir la evaluación.");
    }
}