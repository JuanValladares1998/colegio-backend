package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos de una lección")
public record LeccionResponse(
    Integer       idLeccion,
    Integer       idModulo,
    String        desModulo,
    String        desNombre,
    String        desContenido,
    Short         valOrden,
    Boolean       estObligatoria,
    Boolean       estPublicada,
    Boolean       estActiva,
    LocalDateTime fecCreacion,
    LocalDateTime fecPublicacion
) {}