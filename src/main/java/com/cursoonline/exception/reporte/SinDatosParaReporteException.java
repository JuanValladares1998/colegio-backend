package com.cursoonline.exception.reporte;
public class SinDatosParaReporteException extends RuntimeException {
    public SinDatosParaReporteException() {
        super("No se encontraron registros para los criterios seleccionados, no se puede generar el reporte.");
    }
}