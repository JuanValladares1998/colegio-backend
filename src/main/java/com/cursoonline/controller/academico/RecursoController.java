package com.cursoonline.controller.academico;

import com.cursoonline.dto.academico.request.RecursoRequest;
import com.cursoonline.dto.academico.response.RecursoResponse;
import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.service.academico.RecursoService;
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
import com.cursoonline.service.academico.RecursoService.ArchivoDescarga;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/recursos")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Recursos", description = "Gestión de recursos (ENLACE / VIDEO en este sub-bloque) — CUS-12")
public class RecursoController {

    private final RecursoService recursoService;

    @Operation(summary = "Listar recursos de una lección")
    @GetMapping("/leccion/{idLeccion}")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR', 'ROL_ALUMNO')")
    public ResponseEntity<ApiResponse<List<RecursoResponse>>> listarPorLeccion(
            @PathVariable Integer idLeccion,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.ok(
                ApiResponse.ok("Recursos obtenidos correctamente.",
                        recursoService.listarPorLeccion(idLeccion, usuario))
        );
    }

    @Operation(summary = "Crear recurso de tipo ENLACE o VIDEO — ADMIN o PROFESOR. " +
                         "Para ARCHIVO usar el endpoint de subida (Sub-bloque 3.4).")
    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<RecursoResponse>> crear(
            @Valid @RequestBody RecursoRequest request,
            @AuthenticationPrincipal SegUsuario usuario) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Recurso creado correctamente.",
                        recursoService.crear(request, usuario))
        );
    }

    @Operation(summary = "Eliminar recurso (lógico) — ADMIN o PROFESOR")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) {
        recursoService.eliminar(id, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Recurso eliminado correctamente."));
    }

    @Operation(summary = "Subir archivo como recurso de tipo ARCHIVO — ADMIN o PROFESOR")
    @PostMapping(value = "/archivo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
    public ResponseEntity<ApiResponse<RecursoResponse>> subirArchivo(
            @RequestParam("idLeccion") Integer idLeccion,
            @RequestParam("desNombre") String desNombre,
            @RequestParam("archivo")   MultipartFile archivo,
            @AuthenticationPrincipal SegUsuario usuario) {

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok("Archivo subido y recurso creado correctamente.",
                        recursoService.crearConArchivo(idLeccion, desNombre, archivo, usuario))
        );
    }

    @Operation(summary = "Descargar archivo de un recurso ARCHIVO")
    @GetMapping("/{id}/descargar")
    @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR', 'ROL_ALUMNO')")
    public ResponseEntity<InputStreamResource> descargar(
            @PathVariable Integer id,
            @AuthenticationPrincipal SegUsuario usuario) throws IOException {

        ArchivoDescarga archivo = recursoService.obtenerArchivoParaDescarga(id, usuario);

        InputStream stream = Files.newInputStream(archivo.ruta());
        long contentLength = Files.size(archivo.ruta());

        String filenameEncoded = URLEncoder
                .encode(archivo.nombreOriginal(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + filenameEncoded)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(contentLength)
                .body(new InputStreamResource(stream));
    }
}