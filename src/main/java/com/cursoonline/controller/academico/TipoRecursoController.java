package com.cursoonline.controller.academico;

import com.cursoonline.dto.academico.response.TipoRecursoResponse;
import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.service.academico.TipoRecursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/tipos-recurso")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tipos de Recurso", description = "Catálogo de tipos de recurso (CUS-12)")
public class TipoRecursoController {

    private final TipoRecursoService tipoRecursoService;

    @Operation(summary = "Listar tipos de recurso disponibles")
    @GetMapping
    public ResponseEntity<ApiResponse<List<TipoRecursoResponse>>> listar() {
        return ResponseEntity.ok(
                ApiResponse.ok("Tipos de recurso obtenidos correctamente.",
                        tipoRecursoService.listar())
        );
    }
}