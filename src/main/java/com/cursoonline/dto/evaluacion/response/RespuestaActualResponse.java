package com.cursoonline.dto.evaluacion.response;

public record RespuestaActualResponse(
    Integer idPregunta,
    Integer idOpcionElegida,
    String  desRespuestaTexto
) {}