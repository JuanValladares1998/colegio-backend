package com.cursoonline.exception.usuario;

public class RolNoEncontradoException extends RuntimeException {
    public RolNoEncontradoException(String codRol) {
        super("El rol '" + codRol + "' no existe en el sistema.");
    }
}