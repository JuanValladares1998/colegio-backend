package com.cursoonline.controller.usuario;

import com.cursoonline.dto.common.ApiResponse;
import com.cursoonline.dto.usuario.request.ActualizarEstadoRequest;
import com.cursoonline.dto.usuario.request.ActualizarRolRequest;
import com.cursoonline.dto.usuario.request.CrearUsuarioRequest;
import com.cursoonline.dto.usuario.response.CargaMasivaResponse;
import com.cursoonline.dto.usuario.response.CrearUsuarioResponse;
import com.cursoonline.dto.usuario.response.UsuarioResponse;
import com.cursoonline.service.usuario.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ROL_ADMIN')")
@Tag(name = "Gestión de Usuarios", description = "Endpoints para administrar usuarios del sistema (CUS-05, CUS-07)")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private final UsuarioService usuarioService;

    // ── CUS-05: LISTAR USUARIOS ───────────────────────────────────────────────

    @Operation(
        summary     = "CUS-05 · Listar usuarios",
        description = "Devuelve todos los usuarios registrados con paginación. " +
                      "Parámetros: page (0-based), size, sort."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Lista de usuarios obtenida"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Acceso denegado — requiere ROL_ADMIN"
        )
    })
    @GetMapping
    public ResponseEntity<ApiResponse<Page<UsuarioResponse>>> listar(
            @PageableDefault(size = 10, sort = "desApellidos")
            Pageable pageable) {
        return ResponseEntity.ok(
                ApiResponse.ok("Usuarios obtenidos correctamente.",
                        usuarioService.listarUsuarios(pageable))
        );
    }

    // ── CUS-05: OBTENER USUARIO POR ID ────────────────────────────────────────

    @Operation(
        summary     = "CUS-05 · Obtener usuario por ID",
        description = "Devuelve el detalle de un usuario específico."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Usuario encontrado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Usuario no encontrado"
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UsuarioResponse>> obtener(
            @Parameter(description = "ID del usuario", example = "1")
            @PathVariable Integer id) {
        return ResponseEntity.ok(
                ApiResponse.ok("Usuario obtenido correctamente.",
                        usuarioService.obtenerUsuario(id))
        );
    }

    // ── CUS-05: ACTUALIZAR ROL ────────────────────────────────────────────────

    @Operation(
        summary     = "CUS-05 · Actualizar rol de usuario",
        description = "Cambia el rol de un usuario. Si el admin intenta revocar su propio rol " +
                      "siendo el único admin activo, el sistema rechaza la operación."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Rol actualizado correctamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Rol inválido o auto-revocación bloqueada"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Usuario no encontrado"
        )
    })
    @PutMapping("/{id}/rol")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarRol(
            @Parameter(description = "ID del usuario a modificar", example = "3")
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarRolRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Rol actualizado correctamente. Los permisos han sido modificados.",
                        usuarioService.actualizarRol(id, request))
        );
    }

    // ── CUS-05 A2: ACTIVAR / SUSPENDER CUENTA ────────────────────────────────

    @Operation(
        summary     = "CUS-05 · Activar o suspender cuenta",
        description = "Cambia el estado activo/inactivo de una cuenta. " +
                      "Si se suspende, las sesiones activas del usuario se cierran de inmediato."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Estado actualizado correctamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Usuario no encontrado"
        )
    })
    @PutMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<UsuarioResponse>> actualizarEstado(
            @Parameter(description = "ID del usuario a modificar", example = "3")
            @PathVariable Integer id,
            @Valid @RequestBody ActualizarEstadoRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Estado de cuenta actualizado correctamente.",
                        usuarioService.actualizarEstado(id, request))
        );
    }
     @Operation(
        summary     = "Crear usuario",
        description = "Registra un nuevo usuario alumno con contraseña temporal."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Usuario creado correctamente"),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Datos inválidos o correo ya registrado")
    })
    @PostMapping
    public ResponseEntity<ApiResponse<CrearUsuarioResponse>> crear(
            @Valid @RequestBody CrearUsuarioRequest request) {
        return ResponseEntity.ok(
                ApiResponse.ok("Usuario creado correctamente.",
                        usuarioService.crearUsuario(request))
        );
    }

    // ── CUS-07: CARGA MASIVA ──────────────────────────────────────────────────

    @Operation(
        summary     = "CUS-07 · Carga masiva de alumnos",
        description = "Sube un archivo .xlsx o .csv para registrar múltiples alumnos. " +
                      "El archivo debe tener las columnas: Nombres | Apellidos | Correo. " +
                      "El sistema procesa fila por fila y devuelve un reporte con exitosos y errores."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Archivo procesado — ver reporte en datos"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Formato de archivo inválido o cabeceras incorrectas"
        )
    })
    @PostMapping(value = "/carga-masiva", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<CargaMasivaResponse>> cargaMasiva(
            @Parameter(description = "Archivo .xlsx o .csv con los datos de los alumnos")
            @RequestParam("archivo") MultipartFile archivo) {
        CargaMasivaResponse reporte = usuarioService.cargarMasiva(archivo);
        String mensaje = String.format(
                "Archivo procesado. %d registrados con éxito. %d errores encontrados.",
                reporte.exitosos(), reporte.fallidos());
        return ResponseEntity.ok(ApiResponse.ok(mensaje, reporte));
    }
}