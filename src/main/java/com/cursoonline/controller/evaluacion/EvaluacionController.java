package com.cursoonline.controller.evaluacion;

import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.dto.evaluacion.request.EvaluacionRequest;
import com.cursoonline.dto.evaluacion.response.EvaluacionResponse;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.service.evaluacion.EvaluacionService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/evaluaciones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Evaluaciones", description = "Gestión de evaluaciones (CUS-13)")
@PreAuthorize("hasAnyAuthority('ROL_ADMIN','ROL_PROFESOR')")
public class EvaluacionController {

    private final EvaluacionService service;

    @GetMapping("/modulo/{idModulo}")
    public ResponseEntity<ApiResponse<List<EvaluacionResponse>>> listarPorModulo(
            @PathVariable Integer idModulo,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Evaluaciones obtenidas.", service.listarPorModulo(idModulo, usuario)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluacionResponse>> obtener(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Evaluación obtenida.", service.obtener(id, usuario)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<EvaluacionResponse>> crear(
            @Valid @RequestBody EvaluacionRequest req,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Evaluación creada.", service.crear(req, usuario)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EvaluacionResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody EvaluacionRequest req,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Evaluación actualizada.", service.actualizar(id, req, usuario)));
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<ApiResponse<EvaluacionResponse>> activar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Evaluación activada.", service.activar(id, usuario)));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<ApiResponse<EvaluacionResponse>> desactivar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Evaluación desactivada.", service.desactivar(id, usuario)));
    }
}