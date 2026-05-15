package com.cursoonline.controller.evaluacion;

import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.dto.evaluacion.request.PreguntaRequest;
import com.cursoonline.dto.evaluacion.response.PreguntaResponse;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.service.evaluacion.PreguntaService;
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
@RequestMapping("/preguntas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Preguntas", description = "Banco de preguntas y opciones (CUS-13)")
@PreAuthorize("hasAnyAuthority('ROL_ADMIN','ROL_PROFESOR')")
public class PreguntaController {

    private final PreguntaService service;

    @GetMapping("/evaluacion/{idEvaluacion}")
    public ResponseEntity<ApiResponse<List<PreguntaResponse>>> listarPorEvaluacion(
            @PathVariable Integer idEvaluacion,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Preguntas obtenidas.", service.listarPorEvaluacion(idEvaluacion, usuario)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PreguntaResponse>> obtener(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Pregunta obtenida.", service.obtener(id, usuario)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PreguntaResponse>> crear(
            @Valid @RequestBody PreguntaRequest req,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Pregunta creada.", service.crear(req, usuario)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<PreguntaResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody PreguntaRequest req,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Pregunta actualizada.", service.actualizar(id, req, usuario)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        service.eliminar(id, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Pregunta eliminada."));
    }
}