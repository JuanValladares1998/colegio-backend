package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.util.List;

public record PreguntaParaRendirResponse(
    Integer idPregunta,
    String  codTipo,
    String  desEnunciado,
    Short   valOrden,
    BigDecimal valPuntaje,
    List<OpcionParaRendirResponse> opciones
) {}