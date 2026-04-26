package com.cursoonline.dto.usuario.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Reporte resultado de la carga masiva de alumnos (CUS-07)")
public record CargaMasivaResponse(

    @Schema(description = "Total de filas procesadas en el archivo", example = "30")
    int totalProcesados,

    @Schema(description = "Cantidad de usuarios registrados exitosamente", example = "28")
    int exitosos,

    @Schema(description = "Cantidad de filas que fallaron", example = "2")
    int fallidos,

    @Schema(description = "Credenciales generadas para cada alumno registrado con éxito. " +
                          "El admin debe comunicarlas a cada alumno; cambian en el primer login.")
    List<CredencialAlumno> credenciales,

    @Schema(description = "Lista de errores por fila")
    List<ErrorFila> errores
) {

    @Schema(description = "Credencial generada para un alumno registrado por carga masiva")
    public record CredencialAlumno(

        @Schema(description = "Correo del alumno", example = "pedro@colegio.edu")
        String correo,

        @Schema(description = "Nombres del alumno", example = "Pedro")
        String nombres,

        @Schema(description = "Apellidos del alumno", example = "García")
        String apellidos,

        @Schema(description = "Contraseña temporal generada. Cambio forzado en primer login.",
                example = "Ax9K-2pQ7-vNbM")
        String contrasenaTemporal
    ) {}

    @Schema(description = "Detalle de una fila con error")
    public record ErrorFila(

        @Schema(description = "Número de fila en el archivo (desde 2, sin contar cabecera)", example = "5")
        int fila,

        @Schema(description = "Correo de la fila procesada", example = "juan@colegio.edu")
        String correo,

        @Schema(description = "Motivo del error", example = "El correo ya está registrado.")
        String motivo
    ) {}
}