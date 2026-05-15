package com.cursoonline.dto.evaluacion.request;

public record RespuestaAlumnoRequest(
    Integer idOpcionElegida,   // para OPCION_MULTIPLE y VERDADERO_FALSO
    String  desRespuestaTexto  // para COMPLETAR_CODIGO
) {}