package com.cursoonline.exception.academico;

public class UsuarioNoEsProfesorException extends RuntimeException {
    public UsuarioNoEsProfesorException() {
        super("El usuario seleccionado no tiene el rol de Profesor.");
    }
}