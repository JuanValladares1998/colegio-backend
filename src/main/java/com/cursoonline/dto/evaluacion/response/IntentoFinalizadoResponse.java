package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record IntentoFinalizadoResponse(
    Integer idIntento,
    Integer idEvaluacion,
    String  tituloEvaluacion,
    BigDecimal valCalificacion,
    BigDecimal valPuntajeMinimo,
    Boolean estAprobado,
    Short   numIntento,
    LocalDateTime fecInicio,
    LocalDateTime fecFin
) {}