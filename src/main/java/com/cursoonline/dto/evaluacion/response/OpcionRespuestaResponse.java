package com.cursoonline.dto.evaluacion.response;

public record OpcionRespuestaResponse(
    Integer idOpcion,
    String  desOpcion,
    Boolean estCorrecta,
    Short   valOrden
) {}