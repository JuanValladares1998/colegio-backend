package com.cursoonline.dto.progreso.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Progreso del alumno en un curso (CUS-14)")
public record ProgresoCursoResponse(

    @Schema(description = "ID del curso", example = "3")
    Integer idCurso,

    @Schema(description = "Nombre del curso", example = "Programación I")
    String nombreCurso,

    @Schema(description = "Total de lecciones obligatorias en el curso", example = "20")
    Long totalLeccionesObligatorias,

    @Schema(description = "Lecciones obligatorias completadas", example = "12")
    Long leccionesCompletadas,

    @Schema(description = "Porcentaje de avance (0.0 - 100.0)", example = "60.0")
    Double porcentaje,

    @Schema(description = "Última lección completada (null si no hay actividad)")
    LocalDateTime ultimaActividad,

    @Schema(description = "Avance desglosado por módulo")
    List<ModuloProgresoResponse> modulos,

    @Schema(description = "Boleta de calificaciones (placeholder; se llenará con el Módulo 5)")
    List<Object> calificaciones
) {}