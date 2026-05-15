package com.cursoonline.exception.evaluacion;
public class EvaluacionNoActivaException extends RuntimeException {
    public EvaluacionNoActivaException() {
        super("La evaluación no está activa para los alumnos.");
    }
}