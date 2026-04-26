package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para crear o actualizar una lección")
public record LeccionRequest(

    @Schema(example = "1")
    @NotNull(message = "El módulo es obligatorio.")
    Integer idModulo,

    @Schema(example = "Lección 1: ¿Qué es una variable?")
    @NotBlank(message = "El nombre de la lección es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    String desNombre,

    @Schema(example = "Una variable es un espacio de memoria donde almacenamos un valor...")
    String desContenido,

    @Schema(example = "1")
    @NotNull(message = "El orden es obligatorio.")
    @Min(value = 1, message = "El orden debe ser mayor o igual a 1.")
    Short valOrden,

    @Schema(example = "true")
    @NotNull(message = "Debe indicar si la lección es obligatoria.")
    Boolean estObligatoria
) {}