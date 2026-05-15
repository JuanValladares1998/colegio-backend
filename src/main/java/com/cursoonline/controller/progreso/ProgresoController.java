package com.cursoonline.controller.progreso;

import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.dto.progreso.response.DetalleProgresoAlumnoResponse;
import com.cursoonline.dto.progreso.response.ProgresoCursoResponse;
import com.cursoonline.dto.progreso.response.ProgresoLeccionResponse;
import com.cursoonline.dto.progreso.response.TableroSeccionResponse;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.service.progreso.ProgresoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Pageable;
import java.util.List;

@RestController
@RequestMapping("/progreso")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Progreso", description = "Progreso del alumno en lecciones (CUS-14, 15, 16)")
public class ProgresoController {

    private final ProgresoService service;

    @PostMapping("/leccion/{idLeccion}/completar")
    @PreAuthorize("hasAuthority('ROL_ALUMNO')")
    @Operation(summary = "Marcar una lección como completada (alumno)")
    public ResponseEntity<ApiResponse<ProgresoLeccionResponse>> completarLeccion(
            @PathVariable Integer idLeccion,
            @AuthenticationPrincipal SegUsuario alumno) {

        ProgresoLeccionResponse data = service.marcarLeccionCompletada(idLeccion, alumno);
        return ResponseEntity.ok(
                ApiResponse.ok("Lección marcada como completada.", data));
    }

    @GetMapping("/mi-progreso")
    @PreAuthorize("hasAuthority('ROL_ALUMNO')")
    @Operation(summary = "Listar el progreso del alumno en cada uno de sus cursos (CUS-14)")
    public ResponseEntity<ApiResponse<List<ProgresoCursoResponse>>> miProgreso(
            @AuthenticationPrincipal SegUsuario alumno) {

        List<ProgresoCursoResponse> data = service.obtenerMiProgreso(alumno);
        return ResponseEntity.ok(
                ApiResponse.ok("Progreso obtenido.", data));
    }
     @GetMapping("/seccion/{idSeccion}")
    @PreAuthorize("hasAuthority('ROL_PROFESOR')")
    @Operation(summary = "Tablero de avance grupal de una sección (CUS-15)")
    public ResponseEntity<ApiResponse<TableroSeccionResponse>> tableroSeccion(
            @PathVariable Integer idSeccion,
            @AuthenticationPrincipal SegUsuario profesor,
            @ParameterObject Pageable pageable) {

        TableroSeccionResponse data = service.obtenerTableroSeccion(
                idSeccion, profesor, pageable);
        return ResponseEntity.ok(
                ApiResponse.ok("Tablero obtenido.", data));
    }
    @GetMapping("/alumno/{idAlumno}/curso/{idCurso}")
    @PreAuthorize("hasAuthority('ROL_PROFESOR')")
    @Operation(summary = "Detalle de progreso de un alumno en un curso (CUS-16)")
    public ResponseEntity<ApiResponse<DetalleProgresoAlumnoResponse>> detalleAlumno(
            @PathVariable Integer idAlumno,
            @PathVariable Integer idCurso,
            @AuthenticationPrincipal SegUsuario profesor) {

        DetalleProgresoAlumnoResponse data = service
                .obtenerProgresoAlumno(idAlumno, idCurso, profesor);
        return ResponseEntity.ok(
                ApiResponse.ok("Detalle obtenido.", data));
    }
    
}