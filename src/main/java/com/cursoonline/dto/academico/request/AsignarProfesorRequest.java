package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para asignar un profesor a una sección")
public record AsignarProfesorRequest(

    @Schema(example = "3")
    @NotNull(message = "El ID del profesor es obligatorio.")
    Integer idProfesor
) {}