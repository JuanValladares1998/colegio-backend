package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record IntentoEnCursoResponse(
    Integer idIntento,
    Integer idEvaluacion,
    String  tituloEvaluacion,
    String  desInstrucciones,
    BigDecimal valPuntajeMinimo,
    Short   numIntento,
    Integer minutosRestantes,
    LocalDateTime fecInicio,
    List<PreguntaParaRendirResponse> preguntas,
    List<RespuestaActualResponse> misRespuestas
) {}