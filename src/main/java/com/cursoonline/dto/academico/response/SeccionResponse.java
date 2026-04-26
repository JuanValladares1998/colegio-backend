package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos de una sección")
public record SeccionResponse(
    Integer       idSeccion,
    Integer       idCurso,
    String        desCurso,
    Integer       idAnioEscolar,
    Short         valAnio,
    String        desNombre,
    Boolean       estActivo,
    LocalDateTime fecCreacion,
    // Profesor asignado — null si no tiene
    Integer       idProfesor,
    String        desProfesor
) {}