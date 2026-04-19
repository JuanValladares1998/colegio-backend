package com.cursoonline.dto.auth.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta del restablecimiento de credenciales")
public record RecuperarCredencialesResponse(

    @Schema(description = "Correo del usuario afectado", example = "juan@colegio.edu")
    String correo,

    @Schema(description = "Nueva contraseña temporal generada", example = "aB3xZ9qR2")
    String contrasenaTemporal
) {}