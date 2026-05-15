package com.cursoonline.dto.evaluacion.response;

import java.math.BigDecimal;
import java.util.List;

public record PreguntaRevisionResponse(
    Integer idPregunta,
    String  codTipo,
    String  desEnunciado,
    BigDecimal valPuntaje,
    Integer miOpcionElegidaId,
    String  miRespuestaTexto,
    Boolean miRespuestaCorrecta,
    List<OpcionRespuestaResponse> opciones  // CON estCorrecta (reusa 5.2)
) {}