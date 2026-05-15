package com.cursoonline.dto.evaluacion.response;

public record OpcionParaRendirResponse(
    Integer idOpcion,
    String  desOpcion,
    Short   valOrden
) {}