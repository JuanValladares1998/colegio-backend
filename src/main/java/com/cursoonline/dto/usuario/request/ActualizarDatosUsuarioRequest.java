package com.cursoonline.dto.usuario.request;

import jakarta.validation.constraints.*;

public record ActualizarDatosUsuarioRequest(
    @NotBlank @Size(max = 80) String desNombres,
    @NotBlank @Size(max = 80) String desApellidos,
    @NotBlank @Email @Size(max = 120) String desCorreo
) {}