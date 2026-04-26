package com.cursoonline.controller.academico;

import com.cursoonline.dto.academico.request.LeccionRequest;
import com.cursoonline.dto.academico.response.LeccionResponse;
import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.service.academico.LeccionService;
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
@RequestMapping("/lecciones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Lecciones", description = "Gestión de lecciones (CUS-12)")
public class LeccionController {

    private final LeccionService leccionService;

    @Operation(summary = "Listar lecciones de un módulo (filtrado por rol)")
    @GetMapping("/modulo/{idModulo}")
    public ResponseEntity<ApiResponse<List<LeccionResponse>>> listarPorModulo(
            @PathVariable Integer idModulo,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lecciones obtenidas correctamente.",
                        leccionService.listarPorModulo(idModulo, usuario))
        );
    }

    @Operation(summary = "Obtener lección por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<LeccionResponse>> obtener(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lección obtenida correctamente.",
                        leccionService.obtener(id, usuario))
        );
    }

    @Operation(summary = "Crear lección — ADMIN o PROFESOR (solo cursos asignados)")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<LeccionResponse>> crear(
            @Valid @RequestBody LeccionRequest request,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Lección creada correctamente.",
                        leccionService.crear(request, usuario))
        );
    }

    @Operation(summary = "Actualizar lección — ADMIN o PROFESOR")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<LeccionResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody LeccionRequest request,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lección actualizada correctamente.",
                        leccionService.actualizar(id, request, usuario))
        );
    }

    @Operation(summary = "Publicar lección — ADMIN o PROFESOR")
    @PutMapping("/{id}/publicar")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<LeccionResponse>> publicar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lección publicada correctamente.",
                        leccionService.publicar(id, usuario))
        );
    }

    @Operation(summary = "Despublicar lección — ADMIN o PROFESOR")
    @PutMapping("/{id}/despublicar")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<LeccionResponse>> despublicar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(
                ApiResponse.ok("Lección despublicada correctamente.",
                        leccionService.despublicar(id, usuario))
        );
    }

    @Operation(summary = "Eliminar lección (lógico) — ADMIN o PROFESOR")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        leccionService.eliminar(id, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Lección eliminada correctamente."));
    }
}