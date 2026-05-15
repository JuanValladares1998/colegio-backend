package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EvaluacionResponse(
    Integer idEvaluacion,
    Integer idModulo,
    String  nombreModulo,
    String  desTitulo,
    String  desInstrucciones,
    BigDecimal valPuntajeMinimo,
    Short   valTiempoLimite,
    Short   valMaxIntentos,
    Boolean estActiva,
    LocalDateTime fecCreacion
) {}