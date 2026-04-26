package com.cursoonline.service.auth;

import com.cursoonline.dto.auth.request.*;
import com.cursoonline.dto.auth.response.*;
import com.cursoonline.entity.auth.*;
import com.cursoonline.exception.auth.*;
import com.cursoonline.exception.usuario.CorreoDuplicadoException;
import com.cursoonline.exception.usuario.RolNoEncontradoException;
import com.cursoonline.exception.usuario.UsuarioNoEncontradoException;
import com.cursoonline.repository.auth.*;
import com.cursoonline.security.auth.jwt.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements UserDetailsService {

    private final AuthenticationManager      authManager;
    private final JwtService                 jwtService;
    private final PasswordEncoder            passwordEncoder;
    private final SegUsuarioRepository       usuarioRepository;
    private final SegSesionRepository        sesionRepository;
    private final CatRolRepository           rolRepository;
    private final AudLogAccesoRepository     logAccesoRepository;
    private final AudLogContrasenaRepository logContrasenaRepository;

    @Override
    public UserDetails loadUserByUsername(String correo) throws UsernameNotFoundException {
        return usuarioRepository.findByDesCorreo(correo)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + correo));
    }

    @Transactional
    public AuthResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String ip = extraerIp(httpRequest);

        try {
            authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.correo(), request.contrasena())
            );
        } catch (DisabledException ex) {
            registrarLogAcceso(null, request.correo(), ip, null, false, "Cuenta inactiva");
            throw new UsuarioInactivoException();
        } catch (BadCredentialsException ex) {
            registrarLogAcceso(null, request.correo(), ip, null, false, "Credenciales incorrectas");
            throw new CredencialesInvalidasException();
        }

        SegUsuario usuario = usuarioRepository.findByDesCorreo(request.correo())
                .orElseThrow(CredencialesInvalidasException::new);

        String token = jwtService.generarToken(usuario);

        sesionRepository.save(SegSesion.builder()
                .usuario(usuario)
                .tokJwt(token)
                .desIp(ip)
                .estActiva(true)
                .fecExpiracion(jwtService.extraerExpiracion(token))
                .build());

        usuarioRepository.actualizarUltimoAcceso(usuario.getIdUsuario(), LocalDateTime.now());
        registrarLogAcceso(usuario, request.correo(), ip,
                usuario.getRol().getCodRol(), true, "Login exitoso");

        log.info("Login → {} [{}]", usuario.getDesCorreo(), usuario.getRol().getCodRol());

        return new AuthResponse(
                token,
                "Bearer",
                usuario.getIdUsuario(),
                usuario.getDesNombres(),
                usuario.getDesApellidos(),
                usuario.getDesCorreo(),
                usuario.getRol().getCodRol(),
                usuario.getEstPwdTemporal()
        );
    }

    @Transactional
    public void logout(String token) {
        sesionRepository.cerrarSesionPorToken(token, LocalDateTime.now());
        log.info("Sesión cerrada");
    }

    @Transactional
    public void cambiarContrasena(CambioContrasenaRequest request,
                                   HttpServletRequest httpRequest) {
        String     ip  = extraerIp(httpRequest);
        SegUsuario usr = usuarioAutenticado();

        if (!passwordEncoder.matches(request.contrasenaActual(), usr.getPwdContrasena())) {
            registrarLogContrasena(usr, ip, false, "Contraseña actual incorrecta");
            throw new ContrasenaActualIncorrectaException();
        }

        if (!request.nuevaContrasena().equals(request.confirmarContrasena()))
            throw new ContrasenaNoCoincideException();

        if (passwordEncoder.matches(request.nuevaContrasena(), usr.getPwdContrasena()))
            throw new ContrasenaIgualException();

        usr.setPwdContrasena(passwordEncoder.encode(request.nuevaContrasena()));
        usr.setEstPwdTemporal(false);
        usuarioRepository.save(usr);

        sesionRepository.cerrarSesionesPorUsuario(usr.getIdUsuario(), LocalDateTime.now());
        registrarLogContrasena(usr, ip, true, "Contraseña actualizada correctamente");

        log.info("Contraseña cambiada → {}", usr.getDesCorreo());
    }

    @Transactional
    public RegisterResponse registrarUsuario(RegisterRequest request) {

        if (usuarioRepository.existsByDesCorreo(request.correo()))
            throw new CorreoDuplicadoException(request.correo());

        CatRol rol = rolRepository.findByCodRol(request.codRol())
                .orElseThrow(() -> new RolNoEncontradoException(request.codRol()));

        String contrasenaCruda = (request.contrasenaTemporal() != null
                && !request.contrasenaTemporal().isBlank())
                ? request.contrasenaTemporal()
                : generarContrasenaAleatoria();

        SegUsuario nuevo = SegUsuario.builder()
                .rol(rol)
                .desNombres(request.nombres())
                .desApellidos(request.apellidos())
                .desCorreo(request.correo())
                .pwdContrasena(passwordEncoder.encode(contrasenaCruda))
                .estActivo(true)
                .estPwdTemporal(true)
                .build();

        usuarioRepository.save(nuevo);

        log.info("Usuario registrado → {} | rol: {} | por: {}",
                nuevo.getDesCorreo(), rol.getCodRol(), usuarioAutenticado().getDesCorreo());

        return new RegisterResponse(
                nuevo.getIdUsuario(),
                nuevo.getDesNombres(),
                nuevo.getDesApellidos(),
                nuevo.getDesCorreo(),
                rol.getCodRol(),
                contrasenaCruda
        );
    }

    @Transactional
public String recuperarCredenciales(Integer idUsuario, HttpServletRequest httpRequest) {
    String ip = extraerIp(httpRequest);

    SegUsuario usuario = usuarioRepository.findById(idUsuario)
            .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

    String nueva = generarContrasenaAleatoria();
    usuario.setPwdContrasena(passwordEncoder.encode(nueva));
    usuario.setEstPwdTemporal(true);
    usuarioRepository.save(usuario);

    sesionRepository.cerrarSesionesPorUsuario(idUsuario, LocalDateTime.now());
    registrarLogContrasena(usuario, ip, true, "Restablecida por administrador");

    log.info("Credenciales recuperadas → {} | actor: {}",
            usuario.getDesCorreo(), usuarioAutenticado().getDesCorreo());

    return nueva; // ← ahora devuelve la contraseña
}

    // ── Helpers privados ──────────────────────────────────────────────────────

    private SegUsuario usuarioAutenticado() {
        return (SegUsuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String generarContrasenaAleatoria() {
        byte[] bytes = new byte[9];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String extraerIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }

    private void registrarLogAcceso(SegUsuario usuario, String correo, String ip,
                                     String codRol, boolean exitoso, String detalle) {
        logAccesoRepository.save(AudLogAcceso.builder()
                .usuario(usuario).desCorreo(correo).desIp(ip)
                .codRol(codRol).estExitoso(exitoso).desDetalle(detalle).build());
    }

    private void registrarLogContrasena(SegUsuario usuario, String ip,
                                         boolean exitoso, String detalle) {
        logContrasenaRepository.save(AudLogContrasena.builder()
                .usuario(usuario).desIp(ip).estExitoso(exitoso).desDetalle(detalle).build());
    }
    
}