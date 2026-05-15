package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.util.List;

public record PreguntaResponse(
    Integer idPregunta,
    Integer idEvaluacion,
    Integer idTipoPregunta,
    String  codTipo,
    String  desEnunciado,
    Short   valOrden,
    BigDecimal valPuntaje,
    Boolean estActiva,
    List<OpcionRespuestaResponse> opciones
) {}