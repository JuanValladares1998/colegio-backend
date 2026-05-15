package com.cursoonline.dto.progreso.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos básicos del alumno para la ficha de detalle")
public record AlumnoResumenResponse(
    Integer idAlumno,
    String  nombres,
    String  apellidos,
    String  correo
) {}