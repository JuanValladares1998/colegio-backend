package com.cursoonline.dto.usuario.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Datos necesarios para crear un usuario")
public record CrearUsuarioRequest(

    @Schema(description = "Nombres del usuario", example = "Carlos Alberto")
    @NotBlank
    String nombres,

    @Schema(description = "Apellidos del usuario", example = "Quispe Mamani")
    @NotBlank
    String apellidos,

    @Schema(description = "Correo electrónico", example = "carlos@colegio.edu")
    @Email
    @NotBlank
    String correo

) {}