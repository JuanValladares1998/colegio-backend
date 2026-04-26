package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Datos de un nivel académico")
public record NivelResponse(
    Integer idNivel,
    String  codNivel,
    String  desNombre,
    Short   valOrden,
    Boolean estActivo
) {}