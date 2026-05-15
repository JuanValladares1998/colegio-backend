package com.cursoonline.dto.evaluacion.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record EvaluacionRequest(

    @NotNull(message = "El módulo es obligatorio")
    @Schema(example = "5")
    Integer idModulo,

    @NotBlank(message = "El título es obligatorio")
    @Size(max = 150)
    @Schema(example = "Examen final del módulo")
    String desTitulo,

    @Size(max = 5000)
    String desInstrucciones,

    @NotNull
    @DecimalMin(value = "0.00", message = "El puntaje mínimo no puede ser negativo")
    @DecimalMax(value = "100.00", message = "El puntaje mínimo no puede superar 100")
    @Schema(example = "60.00")
    BigDecimal valPuntajeMinimo,

    @Positive(message = "El tiempo límite debe ser positivo")
    @Schema(description = "Minutos. null = sin límite", example = "30")
    Short valTiempoLimite,

    @NotNull
    @Positive(message = "El número de intentos debe ser positivo")
    @Schema(example = "3")
    Short valMaxIntentos
) {}