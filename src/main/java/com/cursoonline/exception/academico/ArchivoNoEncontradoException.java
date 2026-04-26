package com.cursoonline.exception.academico;

public class ArchivoNoEncontradoException extends RuntimeException {
    public ArchivoNoEncontradoException(String ruta) {
        super("El archivo físico no existe en el servidor: " + ruta);
    }
}