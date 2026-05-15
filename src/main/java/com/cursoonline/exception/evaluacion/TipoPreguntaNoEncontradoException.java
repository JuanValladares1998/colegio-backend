package com.cursoonline.exception.evaluacion;

public class TipoPreguntaNoEncontradoException extends RuntimeException {
    public TipoPreguntaNoEncontradoException(Integer id) {
        super("Tipo de pregunta con ID " + id + " no encontrado.");
    }
}