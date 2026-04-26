package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para crear o actualizar un módulo de curso")
public record ModuloRequest(

    @Schema(example = "1")
    @NotNull(message = "El curso es obligatorio.")
    Integer idCurso,

    @Schema(example = "Módulo 1: Introducción a Java")
    @NotBlank(message = "El nombre del módulo es obligatorio.")
    @Size(max = 120, message = "El nombre no puede superar 120 caracteres.")
    String desNombre,

    @Schema(example = "Conceptos básicos del lenguaje Java y configuración del entorno.")
    String desDescripcion,

    @Schema(example = "1")
    @NotNull(message = "El orden es obligatorio.")
    @Min(value = 1, message = "El orden debe ser mayor o igual a 1.")
    Short valOrden
) {}