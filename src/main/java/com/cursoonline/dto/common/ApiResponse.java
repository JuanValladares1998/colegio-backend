
package com.cursoonline.dto.common;
import io.swagger.v3.oas.annotations.media.Schema;
public record ApiResponse<T>(

    @Schema(description = "true si la operación fue exitosa", example = "true")
    boolean exito,

    @Schema(description = "Mensaje descriptivo del resultado", example = "Login exitoso.")
    String mensaje,

    @Schema(description = "Datos del resultado. null en caso de error.")
    T datos
) {
    public static <T> ApiResponse<T> ok(String mensaje, T datos) {
        return new ApiResponse<>(true, mensaje, datos);
    }

    public static <T> ApiResponse<T> ok(String mensaje) {
        return new ApiResponse<>(true, mensaje, null);
    }

    public static <T> ApiResponse<T> error(String mensaje) {
        return new ApiResponse<>(false, mensaje, null);
    }
}