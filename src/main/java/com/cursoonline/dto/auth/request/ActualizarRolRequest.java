package com.cursoonline.dto.auth.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos para actualizar el rol de un usuario (CUS-05)")
public record ActualizarRolRequest(

    @Schema(
        description     = "Nuevo código de rol a asignar",
        example         = "ROL_PROFESOR",
        allowableValues = {"ROL_ADMIN", "ROL_PROFESOR", "ROL_ALUMNO"}
    )
    @NotBlank(message = "El rol es obligatorio.")
    String codRol
) {}