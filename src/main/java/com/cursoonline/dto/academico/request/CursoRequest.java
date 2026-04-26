package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear o actualizar un curso")
public record CursoRequest(

    @Schema(example = "1")
    @NotNull(message = "El nivel es obligatorio.")
    Integer idNivel,

    @Schema(example = "Introducción a la Programación")
    @NotBlank(message = "El nombre del curso es obligatorio.")
    String desNombre,

    @Schema(example = "Curso de fundamentos de programación")
    String desDescripcion
) {}