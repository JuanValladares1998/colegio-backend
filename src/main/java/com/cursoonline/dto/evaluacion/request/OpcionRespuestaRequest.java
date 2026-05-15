package com.cursoonline.dto.evaluacion.request;

import jakarta.validation.constraints.*;

public record OpcionRespuestaRequest(
    @NotBlank @Size(max = 5000) String desOpcion,
    @NotNull Boolean estCorrecta,
    @NotNull @Positive Short valOrden
) {}