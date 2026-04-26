package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos de un alumno inscrito en una sección")
public record InscripcionAlumnoResponse(
    Integer       idAlumnoSeccion,
    Integer       idUsuario,
    String        desNombres,
    String        desApellidos,
    String        desCorreo,
    Integer       idSeccion,
    String        desSeccion,
    Boolean       estActivo,
    LocalDateTime fecInscripcion
) {}