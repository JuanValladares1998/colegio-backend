package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos de un recurso de una lección")
public record RecursoResponse(
    Integer       idRecurso,
    Integer       idLeccion,
    Integer       idTipoRecurso,
    String        codTipoRecurso,
    String        desTipoRecurso,
    String        desNombre,
    String        urlRuta,
    Boolean       estActivo,
    LocalDateTime fecCreacion
) {}