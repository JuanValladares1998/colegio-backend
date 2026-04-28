package com.cursoonline.dto.usuario.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta al crear un usuario con contraseña temporal")
public record CrearUsuarioResponse(

    @Schema(description = "Datos del usuario creado")
    UsuarioResponse usuario,

    @Schema(description = "Contraseña temporal generada", example = "AbC123XyZ")
    String contrasenaTemporal

) {}