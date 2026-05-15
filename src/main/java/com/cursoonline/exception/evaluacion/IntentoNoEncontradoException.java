package com.cursoonline.exception.evaluacion;
public class IntentoNoEncontradoException extends RuntimeException {
    public IntentoNoEncontradoException(Integer id) {
        super("Intento con ID " + id + " no encontrado.");
    }
}