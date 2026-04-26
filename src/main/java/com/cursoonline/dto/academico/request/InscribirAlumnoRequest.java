package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para inscribir un alumno a una sección")
public record InscribirAlumnoRequest(

    @Schema(example = "5")
    @NotNull(message = "El ID del alumno es obligatorio.")
    Integer idUsuario
) {}