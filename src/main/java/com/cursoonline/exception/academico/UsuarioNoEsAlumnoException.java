package com.cursoonline.exception.academico;

public class UsuarioNoEsAlumnoException extends RuntimeException {
    public UsuarioNoEsAlumnoException(Integer idUsuario) {
        super("El usuario con ID " + idUsuario + " no tiene rol de alumno.");
    }
}