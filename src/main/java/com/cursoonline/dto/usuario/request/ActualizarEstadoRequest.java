package com.cursoonline.dto.usuario.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Datos para activar o suspender una cuenta (CUS-05 A2)")
public record ActualizarEstadoRequest(

    @Schema(description = "true para activar, false para suspender", example = "false")
    @NotNull(message = "El estado es obligatorio.")
    Boolean activo
) {}