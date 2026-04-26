package com.cursoonline.controller.academico;

import com.cursoonline.dto.academico.request.NivelRequest;
import com.cursoonline.dto.academico.response.NivelResponse;
import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.service.academico.NivelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/niveles")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Niveles", description = "Gestión de niveles académicos (CUS-11)")
public class NivelController {

    private final NivelService nivelService;

    @Operation(summary = "Listar niveles activos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<NivelResponse>>> listar() {
        return ResponseEntity.ok(
                ApiResponse.ok("Niveles obtenidos correctamente.", nivelService.listar())
        );
    }

    @Operation(summary = "Obtener nivel por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NivelResponse>> obtener(@PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Nivel obtenido correctamente.", nivelService.obtener(id))
        );
    }

    @Operation(summary = "Crear nivel — Solo ADMIN")
    @PostMapping
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<NivelResponse>> crear(
            @Valid @RequestBody NivelRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Nivel creado correctamente.", nivelService.crear(request))
        );
    }

    @Operation(summary = "Actualizar nivel — Solo ADMIN")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<NivelResponse>> actualizar(
            @PathVariable Integer id,
            @Valid @RequestBody NivelRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Nivel actualizado correctamente.", nivelService.actualizar(id, request))
        );
    }
}