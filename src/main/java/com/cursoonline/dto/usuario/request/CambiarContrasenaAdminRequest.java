package com.cursoonline.dto.usuario.request;

import jakarta.validation.constraints.*;

public record CambiarContrasenaAdminRequest(
    @NotBlank
    @Size(min = 8, max = 100, message = "La contraseña debe tener entre 8 y 100 caracteres")
    String pwdNueva
) {}