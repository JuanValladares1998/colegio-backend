package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para crear o actualizar un nivel académico")
public record NivelRequest(

    @Schema(example = "BASICO")
    @NotBlank(message = "El código del nivel es obligatorio.")
    String codNivel,

    @Schema(example = "Nivel Básico")
    @NotBlank(message = "El nombre del nivel es obligatorio.")
    String desNombre,

    @Schema(example = "1")
    @NotNull(message = "El orden es obligatorio.")
    Short valOrden
) {}