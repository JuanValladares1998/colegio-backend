package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos de un módulo de curso")
public record ModuloResponse(
    Integer       idModulo,
    Integer       idCurso,
    String        desCurso,
    String        desNombre,
    String        desDescripcion,
    Short         valOrden,
    Boolean       estActivo,
    LocalDateTime fecCreacion
) {}