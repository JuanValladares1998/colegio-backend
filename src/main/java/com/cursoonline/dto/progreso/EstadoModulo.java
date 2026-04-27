package com.cursoonline.dto.progreso;
 
import io.swagger.v3.oas.annotations.media.Schema;
 
@Schema(description = "Estado de avance de un módulo dentro de un curso")
public enum EstadoModulo {
    COMPLETADO,
    EN_CURSO,
    PENDIENTE
}