package com.cursoonline.exception.usuario;

public class UsuarioNoEncontradoException extends RuntimeException {
    public UsuarioNoEncontradoException(Integer id) {
        super("Usuario con ID " + id + " no encontrado.");
    }
}