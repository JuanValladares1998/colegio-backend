package com.cursoonline.exception.evaluacion;
public class IntentoCompletadoException extends RuntimeException {
    public IntentoCompletadoException() {
        super("El intento ya está finalizado y no admite cambios.");
    }
}