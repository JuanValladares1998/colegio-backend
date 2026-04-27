package com.cursoonline.dto.progreso.response;
 
import io.swagger.v3.oas.annotations.media.Schema;
 
import java.time.LocalDateTime;
 
@Schema(description = "Resultado de marcar una lección como completada")
public record ProgresoLeccionResponse(
 
    @Schema(description = "ID del registro de progreso", example = "42")
    Integer idProgreso,
 
    @Schema(description = "ID de la lección", example = "12")
    Integer idLeccion,
 
    @Schema(description = "Nombre de la lección", example = "Variables y tipos en Java")
    String nombreLeccion,
 
    @Schema(description = "true si la lección quedó como completada", example = "true")
    Boolean estCompletada,
 
    @Schema(description = "Fecha en que se marcó la finalización")
    LocalDateTime fecCompletado
) {}