package com.cursoonline.dto.academico.response;

import java.time.LocalDate;

public record ResumenAnioEscolarResponse(
    Integer idAnioEscolar,
    Short   valAnio,
    String  desDescripcion,
    LocalDate fecInicio,
    LocalDate fecFin,
    Boolean estActivo,
    long    totalSecciones,
    long    totalCursos,
    long    totalAlumnosInscritos,
    long    totalProfesoresAsignados
) {}