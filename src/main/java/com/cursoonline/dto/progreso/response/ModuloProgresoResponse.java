package com.cursoonline.dto.progreso.response;
 
import com.cursoonline.dto.progreso.EstadoModulo;
import io.swagger.v3.oas.annotations.media.Schema;
 
@Schema(description = "Estado de avance de un módulo del curso")
public record ModuloProgresoResponse(
 
    @Schema(description = "ID del módulo", example = "5")
    Integer idModulo,
 
    @Schema(description = "Nombre del módulo", example = "Introducción a Java")
    String nombre,
 
    @Schema(description = "Estado calculado del módulo")
    EstadoModulo estado,
 
    @Schema(description = "Lecciones obligatorias completadas en el módulo", example = "3")
    Long completadas,
 
    @Schema(description = "Total de lecciones obligatorias del módulo", example = "5")
    Long total
) {}
 