package com.cursoonline.exception.evaluacion;


public class PreguntaNoEncontradaException extends RuntimeException {
    public PreguntaNoEncontradaException(Integer id) {
        super("Pregunta con ID " + id + " no encontrada.");
    }
}