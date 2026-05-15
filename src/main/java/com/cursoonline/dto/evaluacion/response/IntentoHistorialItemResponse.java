package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IntentoHistorialItemResponse(
    Integer idIntento,
    Short   numIntento,
    BigDecimal valCalificacion,
    Boolean estAprobado,
    Boolean estCompletado,
    LocalDateTime fecInicio,
    LocalDateTime fecFin
) {}