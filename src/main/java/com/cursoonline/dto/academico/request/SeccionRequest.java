package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear o actualizar una sección")
public record SeccionRequest(

    @Schema(example = "1")
    @NotNull(message = "El curso es obligatorio.")
    Integer idCurso,

    @Schema(example = "1")
    @NotNull(message = "El año escolar es obligatorio.")
    Integer idAnioEscolar,

    @Schema(example = "Taller de Programación - A")
    @NotBlank(message = "El nombre de la sección es obligatorio.")
    String desNombre
) {}