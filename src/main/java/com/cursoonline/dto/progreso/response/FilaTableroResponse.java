package com.cursoonline.dto.progreso.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Fila del tablero grupal — un alumno de la sección")
public record FilaTableroResponse(
    Integer idAlumno,
    String  nombres,
    String  apellidos,
    Long    leccionesCompletadas,
    Long    totalLeccionesObligatorias,
    Double  porcentaje,
    LocalDateTime ultimaActividad
) {}