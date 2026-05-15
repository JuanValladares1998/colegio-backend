package com.cursoonline.dto.progreso.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Módulo con sus lecciones para el detalle de un alumno")
public record ModuloDetalleResponse(
    Integer idModulo,
    String  nombre,
    List<LeccionEstadoResponse> lecciones
) {}