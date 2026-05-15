package com.cursoonline.dto.progreso.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Estado de una lección dentro del historial de un alumno")
public record LeccionEstadoResponse(
    Integer idLeccion,
    String  nombre,
    Boolean obligatoria,
    Boolean completada,
    LocalDateTime fecCompletado
) {}