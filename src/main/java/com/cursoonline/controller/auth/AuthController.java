package com.cursoonline.controller.auth;

import com.cursoonline.dto.auth.request.*;
import com.cursoonline.dto.auth.response.*;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.exception.auth.UsuarioNoEncontradoException;
import com.cursoonline.repository.auth.SegUsuarioRepository;
import com.cursoonline.service.auth.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints de login, logout, contraseña y registro de usuarios")
public class AuthController {

    private final AuthService authService;
    private final SegUsuarioRepository usuarioRepository;

    // ── CUS-01: LOGIN ─────────────────────────────────────────────────────────

    @Operation(
        summary     = "CUS-01 · Iniciar sesión",
        description = "Autentica al usuario con correo y contraseña. Devuelve un token JWT y los datos básicos del perfil."
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Login exitoso",
            content = @Content(schema = @Schema(implementation = AuthResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Campos vacíos o formato inválido"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Credenciales incorrectas"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Cuenta inactiva"
        )
    })
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        return ResponseEntity.ok(
                ApiResponse.ok("Login exitoso.", authService.login(request, httpRequest))
        );
    }

    // ── CUS-02: LOGOUT ────────────────────────────────────────────────────────

    @Operation(
        summary     = "CUS-02 · Cerrar sesión",
        description = "Invalida el token JWT activo en la base de datos. El token queda inutilizable aunque no haya expirado.",
        security    = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Sesión cerrada correctamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Token inválido o no enviado"
        )
    })
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest httpRequest) {
        authService.logout(extraerToken(httpRequest));
        return ResponseEntity.ok(ApiResponse.ok("Sesión cerrada correctamente."));
    }

    // ── CUS-03: CAMBIAR CONTRASEÑA ────────────────────────────────────────────

    @Operation(
        summary     = "CUS-03 · Cambiar contraseña",
        description = "Permite al usuario autenticado cambiar su contraseña. Invalida todas sus sesiones activas tras el cambio.",
        security    = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Contraseña actualizada correctamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400",
            description  = "Contraseñas no coinciden | Igual a la actual | No cumple la política"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "401", description = "Contraseña actual incorrecta"
        )
    })
    @PutMapping("/cambiar-contrasena")
    public ResponseEntity<ApiResponse<Void>> cambiarContrasena(
            @Valid @RequestBody CambioContrasenaRequest request,
            HttpServletRequest httpRequest) {
        authService.cambiarContrasena(request, httpRequest);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña actualizada correctamente."));
    }

    // ── CUS-06: REGISTRAR USUARIO ─────────────────────────────────────────────

    @Operation(
        summary     = "CUS-06 · Registrar usuario",
        description = "Crea un nuevo usuario en el sistema. Solo accesible para ROL_ADMIN. Si no se provee contraseña, el sistema genera una temporal.",
        security    = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "201", description = "Usuario registrado correctamente",
            content = @Content(schema = @Schema(implementation = RegisterResponse.class))
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "400", description = "Campos inválidos o rol no existe"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Acceso denegado — requiere ROL_ADMIN"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "409", description = "El correo ya está registrado"
        )
    })
    @PostMapping("/registrar")
    @PreAuthorize("hasAuthority('ROL_ADMIN')")
    public ResponseEntity<ApiResponse<RegisterResponse>> registrar(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.ok(
                    "Usuario registrado correctamente. Las credenciales temporales han sido generadas.",
                    authService.registrarUsuario(request)
                )
        );
    }

    // ── CUS-04: RECUPERAR CREDENCIALES ────────────────────────────────────────

    @Operation(
        summary     = "CUS-04 · Recuperar credenciales",
        description = "Restablece la contraseña de un usuario generando una nueva temporal. Accesible para ROL_ADMIN y ROL_PROFESOR.",
        security    = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "200", description = "Credenciales restablecidas correctamente"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "403", description = "Acceso denegado"
        ),
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
            responseCode = "404", description = "Usuario no encontrado"
        )
    })
    @PutMapping("/recuperar-credenciales/{id}")
        @PreAuthorize("hasAnyAuthority('ROL_ADMIN', 'ROL_PROFESOR')")
        public ResponseEntity<ApiResponse<RecuperarCredencialesResponse>> recuperarCredenciales(
                @PathVariable Integer id,
                HttpServletRequest httpRequest) {

            String nuevaContrasena = authService.recuperarCredenciales(id, httpRequest);

            // Obtener correo del usuario para incluirlo en el response
            SegUsuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new UsuarioNoEncontradoException(id));

            return ResponseEntity.ok(
                ApiResponse.ok(
                    "Credenciales restablecidas correctamente.",
                    new RecuperarCredencialesResponse(usuario.getDesCorreo(), nuevaContrasena)
                )
            );
        }
    // ── Helper ────────────────────────────────────────────────────────────────

    private String extraerToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        return (header != null && header.startsWith("Bearer ")) ? header.substring(7) : "";
    }
}