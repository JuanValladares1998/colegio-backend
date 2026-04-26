package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos de un curso")
public record CursoResponse(
    Integer       idCurso,
    Integer       idNivel,
    String        desNivel,
    String        desNombre,
    String        desDescripcion,
    Boolean       estPublicado,
    Boolean       estActivo,
    LocalDateTime fecCreacion,
    LocalDateTime fecPublicacion
) {}