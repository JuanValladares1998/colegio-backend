package com.cursoonline.exception.reporte;
public class ErrorGenerandoReporteException extends RuntimeException {
    public ErrorGenerandoReporteException(Throwable cause) {
        super("Hubo un problema al generar el documento. Por favor, intente reducir el rango de fechas o contacte al administrador.", cause);
    }
}