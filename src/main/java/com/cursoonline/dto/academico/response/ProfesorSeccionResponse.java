package com.cursoonline.dto.academico.response;

import java.time.LocalDateTime;

public record ProfesorSeccionResponse(
    Integer idProfesorSeccion,
    Integer idProfesor,
    String  nombresProfesor,
    String  apellidosProfesor,
    String  correoProfesor,
    Integer idSeccion,
    String  nombreSeccion,
    Integer idCurso,
    String  nombreCurso,
    Integer idAnioEscolar,
    Short   valAnio,
    LocalDateTime fecAsignacion
) {}