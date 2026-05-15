package com.cursoonline.controller.evaluacion;

import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.dto.evaluacion.request.RespuestaAlumnoRequest;
import com.cursoonline.dto.evaluacion.response.*;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.service.evaluacion.IntentoService;
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
@RequestMapping("/intentos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Intentos de Evaluación", description = "Rendición de evaluaciones (CUS-13)")
@PreAuthorize("hasAuthority('ROL_ALUMNO')")
public class IntentoController {

    private final IntentoService service;

    @PostMapping("/evaluacion/{idEvaluacion}/iniciar")
    public ResponseEntity<ApiResponse<IntentoEnCursoResponse>> iniciar(
            @PathVariable Integer idEvaluacion,
            @AuthenticationPrincipal SegUsuario alumno) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Intento iniciado/reanudado.", service.iniciarOReanudar(idEvaluacion, alumno)));
    }

    @GetMapping("/{idIntento}")
    public ResponseEntity<ApiResponse<IntentoEnCursoResponse>> obtenerEnCurso(
            @PathVariable Integer idIntento,
            @AuthenticationPrincipal SegUsuario alumno) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Intento obtenido.", service.obtenerIntentoEnCurso(idIntento, alumno)));
    }

    @PutMapping("/{idIntento}/pregunta/{idPregunta}")
    public ResponseEntity<ApiResponse<Void>> guardarRespuesta(
            @PathVariable Integer idIntento,
            @PathVariable Integer idPregunta,
            @Valid @RequestBody RespuestaAlumnoRequest req,
            @AuthenticationPrincipal SegUsuario alumno) {
        service.guardarRespuesta(idIntento, idPregunta, req, alumno);
        return ResponseEntity.ok(ApiResponse.ok("Respuesta guardada."));
    }

    @PostMapping("/{idIntento}/finalizar")
    public ResponseEntity<ApiResponse<IntentoFinalizadoResponse>> finalizar(
            @PathVariable Integer idIntento,
            @AuthenticationPrincipal SegUsuario alumno) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Intento finalizado.", service.finalizar(idIntento, alumno)));
    }

    @GetMapping("/evaluacion/{idEvaluacion}/historial")
    public ResponseEntity<ApiResponse<List<IntentoHistorialItemResponse>>> historial(
            @PathVariable Integer idEvaluacion,
            @AuthenticationPrincipal SegUsuario alumno) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Historial obtenido.", service.obtenerHistorial(idEvaluacion, alumno)));
    }

    @GetMapping("/{idIntento}/revision")
    public ResponseEntity<ApiResponse<IntentoRevisionResponse>> revision(
            @PathVariable Integer idIntento,
            @AuthenticationPrincipal SegUsuario alumno) {
        return ResponseEntity.ok(ApiResponse.ok(
                "Revisión obtenida.", service.obtenerRevision(idIntento, alumno)));
    }
}