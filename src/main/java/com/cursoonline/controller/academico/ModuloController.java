package com.cursoonline.controller.academico;

import com.cursoonline.dto.academico.request.ModuloRequest;
import com.cursoonline.dto.academico.response.ModuloResponse;
import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.service.academico.ModuloService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/modulos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Módulos", description = "Gestión de módulos de cursos (CUS-12)")
public class ModuloController {

    private final ModuloService moduloService;

    @Operation(summary = "Listar módulos de un curso")
    @GetMapping("/curso/{idCurso}")
    public ResponseEntity<ApiResponse<List<ModuloResponse>>> listarPorCurso(
            @PathVariable Integer idCurso) {
        return ResponseEntity.ok(
                ApiResponse.ok("Módulos obtenidos correctamente.",
                        moduloService.listarPorCurso(idCurso))
        );
    }

    @Operation(summary = "Obtener módulo por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModuloResponse>> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Módulo obtenido correctamente.", moduloService.obtener(id))
        );
    }

    @Operation(summary = "Crear módulo — ADMIN o PROFESOR (solo cursos asignados)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<ModuloResponse>> crear(
            @Valid @RequestBody ModuloRequest request,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Módulo creado correctamente.",
                        moduloService.crear(request, usuario))
        );
    }

    @Operation(summary = "Actualizar módulo — ADMIN o PROFESOR (solo cursos asignados)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<ModuloResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody ModuloRequest request,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(
                ApiResponse.ok("Módulo actualizado correctamente.",
                        moduloService.actualizar(id, request, usuario))
        );
    }

    @Operation(summary = "Eliminar módulo (lógico) — ADMIN o PROFESOR (solo cursos asignados)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        moduloService.eliminar(id, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Módulo eliminado correctamente."));
    }
}