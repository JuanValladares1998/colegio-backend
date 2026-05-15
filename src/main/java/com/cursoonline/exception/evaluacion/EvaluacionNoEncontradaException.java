package com.cursoonline.exception.evaluacion;

public class EvaluacionNoEncontradaException extends RuntimeException {
    public EvaluacionNoEncontradaException(Integer id) {
        super("Evaluación con ID " + id + " no encontrada.");
    }
}