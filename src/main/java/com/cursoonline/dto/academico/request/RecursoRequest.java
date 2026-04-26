package com.cursoonline.dto.academico.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "Datos para crear un recurso. " +
        "Para ENLACE y VIDEO la URL es obligatoria. " +
        "Para ARCHIVO la URL se completa al subir el archivo.")
public record RecursoRequest(

    @Schema(example = "1")
    @NotNull(message = "La lección es obligatoria.")
    Integer idLeccion,

    @Schema(example = "2", description = "1=ARCHIVO, 2=ENLACE, 3=VIDEO")
    @NotNull(message = "El tipo de recurso es obligatorio.")
    Integer idTipoRecurso,

    @Schema(example = "Tutorial introductorio")
    @NotBlank(message = "El nombre del recurso es obligatorio.")
    @Size(max = 150, message = "El nombre no puede superar 150 caracteres.")
    String desNombre,

    @Schema(example = "https://www.youtube.com/watch?v=abc123",
            description = "Obligatoria para ENLACE y VIDEO. Ignorada para ARCHIVO.")
    String urlRuta
) {}