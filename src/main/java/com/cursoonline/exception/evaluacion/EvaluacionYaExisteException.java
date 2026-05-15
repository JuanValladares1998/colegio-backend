package com.cursoonline.exception.evaluacion;

public class EvaluacionYaExisteException extends RuntimeException {
    public EvaluacionYaExisteException(String titulo) {
        super("Ya existe una evaluación con el título '" + titulo + "' en este módulo.");
    }
}