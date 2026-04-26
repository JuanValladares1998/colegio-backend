package com.cursoonline.exception.academico;

public class AccesoCursoDenegadoException extends RuntimeException {
    public AccesoCursoDenegadoException() {
        super("No tienes permiso para gestionar contenido de este curso.");
    }
}