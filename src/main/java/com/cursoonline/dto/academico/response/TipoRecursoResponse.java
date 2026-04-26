package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Tipo de recurso disponible (ARCHIVO, ENLACE, VIDEO)")
public record TipoRecursoResponse(
    Integer idTipoRecurso,
    String  codTipo,
    String  desNombre,
    Boolean estActivo
) {}