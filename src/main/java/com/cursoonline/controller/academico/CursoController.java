package com.cursoonline.controller.academico;

import com.cursoonline.dto.academico.request.CursoRequest;
import com.cursoonline.dto.academico.response.CursoResponse;
import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.service.academico.CursoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.cursoonline.entity.auth.SegUsuario;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
@RestController
@RequestMapping("/cursos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Cursos", description = "Gestión de cursos académicos (CUS-11)")
public class CursoController {

    private final CursoService cursoService;

    @Operation(summary = "Listar todos los cursos activos — ADMIN")
    @GetMapping
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<Page<CursoResponse>>> listar(
            @PageableDefault(size = 10, sort = "desNombre") Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.ok("Cursos obtenidos correctamente.", cursoService.listar(pageable))
        );
    }

    @Operation(summary = "Listar cursos publicados — Todos los roles")
    @GetMapping("/publicados")
    public ResponseEntity<ApiResponse<Page<CursoResponse>>> listarPublicados(
            @PageableDefault(size = 10, sort = "desNombre") Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.ok("Cursos publicados obtenidos.", cursoService.listarPublicados(pageable))
        );
    }

    @Operation(summary = "Obtener curso por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CursoResponse>> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Curso obtenido correctamente.", cursoService.obtener(id))
        );
    }

    @Operation(summary = "Crear curso — Solo ADMIN")
    @PostMapping
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<CursoResponse>> crear(
            @Valid @RequestBody CursoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Curso creado correctamente.", cursoService.crear(request))
        );
    }

    @Operation(summary = "Actualizar curso — Solo ADMIN")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<CursoResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody CursoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Curso actualizado correctamente.", cursoService.actualizar(id, request))
        );
    }

    @Operation(summary = "Publicar curso — Solo ADMIN")
    @PutMapping("/{id}/publicar")
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<CursoResponse>> publicar(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Curso publicado correctamente.", cursoService.publicar(id))
        );
    }

    @Operation(summary = "Despublicar curso — Solo ADMIN")
    @PutMapping("/{id}/despublicar")
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<CursoResponse>> despublicar(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Curso despublicado correctamente.", cursoService.despublicar(id))
        );
    }
    @Operation(
    summary     = "CUS-11 · Mis cursos — Solo ALUMNO",
    description = "Devuelve los cursos publicados de las secciones donde el alumno autenticado está inscrito activamente."
)
@GetMapping("/mis-cursos")
@PreAuthorize("hasAuthority('ROL_ALUMNO')")
public ResponseEntity<ApiResponse<Page<CursoResponse>>> misCursos(
        @AuthenticationPrincipal SegUsuario alumno,
        @PageableDefault(size = 10, sort = "desNombre") Pageable pageable) {

    return ResponseEntity.ok(
            ApiResponse.ok(
                "Cursos del alumno obtenidos correctamente.",
                cursoService.listarPublicadosPorAlumno(alumno.getIdUsuario(), pageable)
            )
    );
}
}