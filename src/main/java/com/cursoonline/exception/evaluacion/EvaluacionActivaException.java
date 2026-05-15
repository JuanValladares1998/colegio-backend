package com.cursoonline.exception.evaluacion;

public class EvaluacionActivaException extends RuntimeException {
    public EvaluacionActivaException() {
        super("La evaluación está activa. Desactívela primero para modificarla.");
    }
}