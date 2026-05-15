package com.cursoonline.controller.evaluacion;

import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.dto.evaluacion.response.TipoPreguntaResponse;
import com.cursoonline.service.evaluacion.PreguntaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/tipos-pregunta")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Tipos de Pregunta")
public class TipoPreguntaController {

    private final PreguntaService service;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<TipoPreguntaResponse>>> listar() {
        return ResponseEntity.ok(ApiResponse.ok("Tipos obtenidos.", service.listarTipos()));
    }
}