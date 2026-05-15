package com.cursoonline.dto.progreso.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Ficha detallada de progreso de un alumno en un curso (CUS-16)")
public record DetalleProgresoAlumnoResponse(
    AlumnoResumenResponse alumno,
    Integer idCurso,
    String  nombreCurso,
    Long    totalLeccionesObligatorias,
    Long    leccionesCompletadas,
    Double  porcentajeAvance,
    LocalDateTime ultimaActividad,
    List<ModuloDetalleResponse> modulos,
    List<Object> evaluaciones
) {}