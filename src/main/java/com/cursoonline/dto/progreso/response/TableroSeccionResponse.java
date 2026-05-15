package com.cursoonline.dto.progreso.response;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

@Schema(description = "Tablero de avance grupal de una sección (CUS-15)")
public record TableroSeccionResponse(
    Integer idSeccion,
    String  nombreSeccion,
    Integer idCurso,
    String  nombreCurso,
    Long    totalLeccionesObligatorias,
    Page<FilaTableroResponse> alumnos
) {}