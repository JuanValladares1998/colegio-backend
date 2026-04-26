package com.cursoonline.exception.usuario;

public class CorreoDuplicadoException extends RuntimeException {
    public CorreoDuplicadoException(String correo) {
        super("El correo '" + correo + "' ya está asociado a otra cuenta activa.");
    }
}