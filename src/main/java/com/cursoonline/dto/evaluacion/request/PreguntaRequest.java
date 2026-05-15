package com.cursoonline.dto.evaluacion.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.List;

public record PreguntaRequest(
    @NotNull Integer idEvaluacion,
    @NotNull Integer idTipoPregunta,
    @NotBlank @Size(max = 5000) String desEnunciado,
    @NotNull @Positive Short valOrden,

    @NotNull
    @DecimalMin(value = "0.01", message = "El puntaje debe ser positivo")
    @DecimalMax(value = "100.00")
    @Schema(example = "1.00")
    BigDecimal valPuntaje,

    @NotEmpty @Valid List<OpcionRespuestaRequest> opciones
) {}