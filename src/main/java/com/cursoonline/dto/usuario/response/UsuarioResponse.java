package com.cursoonline.dto.usuario.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "Datos de un usuario del sistema")
public record UsuarioResponse(

    @Schema(description = "ID del usuario", example = "1")
    Integer idUsuario,

    @Schema(description = "Nombres del usuario", example = "Carlos Alberto")
    String nombres,

    @Schema(description = "Apellidos del usuario", example = "Quispe Mamani")
    String apellidos,

    @Schema(description = "Correo electrónico", example = "carlos@colegio.edu")
    String correo,

    @Schema(description = "Rol actual", example = "ROL_ALUMNO")
    String rol,

    @Schema(description = "true si la cuenta está activa", example = "true")
    Boolean activo,

    @Schema(description = "true si tiene contraseña temporal", example = "false")
    Boolean pwdTemporal,

    @Schema(description = "Fecha de creación de la cuenta")
    LocalDateTime fecCreacion,

    @Schema(description = "Último acceso al sistema")
    LocalDateTime fecUltimoAcceso
) {}