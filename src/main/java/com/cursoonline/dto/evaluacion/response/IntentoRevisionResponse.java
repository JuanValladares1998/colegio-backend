package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IntentoRevisionResponse(
    Integer idIntento,
    Integer idEvaluacion,
    String  tituloEvaluacion,
    BigDecimal valCalificacion,
    BigDecimal valPuntajeMinimo,
    Boolean estAprobado,
    Short   numIntento,
    LocalDateTime fecInicio,
    LocalDateTime fecFin,
    List<PreguntaRevisionResponse> preguntas
) {}