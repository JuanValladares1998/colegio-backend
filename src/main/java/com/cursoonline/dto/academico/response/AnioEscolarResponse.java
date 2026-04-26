package com.cursoonline.dto.academico.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Datos de un año escolar")
public record AnioEscolarResponse(
    Integer   idAnioEscolar,
    Short     valAnio,
    String    desDescripcion,
    Boolean   estActivo,
    LocalDate fecInicio,
    LocalDate fecFin
) {}