package com.cursoonline.service.usuario;

import com.cursoonline.dto.usuario.request.ActualizarEstadoRequest;
import com.cursoonline.dto.usuario.request.ActualizarRolRequest;
import com.cursoonline.dto.usuario.request.CrearUsuarioRequest;
import com.cursoonline.dto.usuario.response.CargaMasivaResponse;
import com.cursoonline.dto.usuario.response.UsuarioResponse;
import com.cursoonline.dto.usuario.response.CargaMasivaResponse.CredencialAlumno;
import com.cursoonline.dto.usuario.response.CargaMasivaResponse.ErrorFila;
import com.cursoonline.entity.auth.CatRol;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.entity.usuario.AudLogAdmin;
import com.cursoonline.exception.usuario.RolNoEncontradoException;
import com.cursoonline.exception.usuario.UsuarioNoEncontradoException;
import com.cursoonline.dto.usuario.response.CrearUsuarioResponse;
import com.cursoonline.repository.auth.CatRolRepository;
import com.cursoonline.repository.auth.SegSesionRepository;
import com.cursoonline.repository.auth.SegUsuarioRepository;
import com.cursoonline.repository.usuario.AudLogAdminRepository;
import com.opencsv.CSVReader;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final SegUsuarioRepository  usuarioRepository;
    private final CatRolRepository      rolRepository;
    private final SegSesionRepository   sesionRepository;
    private final AudLogAdminRepository logAdminRepository;
    private final PasswordEncoder       passwordEncoder;

    // ── CUS-05: LISTAR USUARIOS (paginado) ───────────────────────────────────

    public Page<UsuarioResponse> listarUsuarios(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(this::toResponse);
    }

    // ── CUS-05: OBTENER USUARIO POR ID ────────────────────────────────────────

    public UsuarioResponse obtenerUsuario(Integer id) {
        return toResponse(
                usuarioRepository.findById(id)
                        .orElseThrow(() -> new UsuarioNoEncontradoException(id))
        );
    }

    // ── CUS-05: ACTUALIZAR ROL ────────────────────────────────────────────────

    @Transactional
    public UsuarioResponse actualizarRol(Integer idUsuario, ActualizarRolRequest request) {
        SegUsuario admin   = adminAutenticado();
        SegUsuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

        // Bloqueo CUS-05 E1: el admin no puede revocar su propio rol si es el único admin
        if (admin.getIdUsuario().equals(idUsuario)
                && admin.getRol().getCodRol().equals("ROL_ADMIN")
                && !request.codRol().equals("ROL_ADMIN")) {

            long totalAdmins = usuarioRepository.findAll().stream()
                    .filter(u -> u.getRol().getCodRol().equals("ROL_ADMIN")
                            && Boolean.TRUE.equals(u.getEstActivo()))
                    .count();

            if (totalAdmins <= 1) {
                throw new IllegalStateException(
                        "Acción denegada. No puede revocar su rol de administrador " +
                        "porque el sistema quedaría sin gestión.");
            }
        }

        CatRol nuevoRol = rolRepository.findByCodRol(request.codRol())
                .orElseThrow(() -> new RolNoEncontradoException(request.codRol()));

        String rolAnterior = usuario.getRol().getCodRol();
        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);

        // Cerrar sesiones activas para que el nuevo rol se aplique inmediatamente
        sesionRepository.cerrarSesionesPorUsuario(idUsuario, LocalDateTime.now());

        registrarLogAdmin(admin, usuario, "CAMBIO_ROL",
                "Rol cambiado de " + rolAnterior + " a " + nuevoRol.getCodRol());

        log.info("Rol actualizado → usuario: {} | {} → {} | admin: {}",
                usuario.getDesCorreo(), rolAnterior, nuevoRol.getCodRol(), admin.getDesCorreo());

        return toResponse(usuario);
    }

    // ── CUS-05: ACTUALIZAR ESTADO (activar / suspender) ──────────────────────

    @Transactional
    public UsuarioResponse actualizarEstado(Integer idUsuario, ActualizarEstadoRequest request) {
        SegUsuario admin   = adminAutenticado();
        SegUsuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new UsuarioNoEncontradoException(idUsuario));

        usuario.setEstActivo(request.activo());
        usuarioRepository.save(usuario);

        // Si se suspende, cerrar sesiones activas de inmediato
        if (Boolean.FALSE.equals(request.activo())) {
            sesionRepository.cerrarSesionesPorUsuario(idUsuario, LocalDateTime.now());
        }

        String accion = Boolean.TRUE.equals(request.activo()) ? "ACTIVAR_CUENTA" : "SUSPENDER_CUENTA";
        registrarLogAdmin(admin, usuario, accion,
                "Cuenta " + (Boolean.TRUE.equals(request.activo()) ? "activada" : "suspendida"));

        log.info("Estado actualizado → usuario: {} | activo: {} | admin: {}",
                usuario.getDesCorreo(), request.activo(), admin.getDesCorreo());

        return toResponse(usuario);
    }
    @Transactional
    public CrearUsuarioResponse crearUsuario(CrearUsuarioRequest request) {

        if (usuarioRepository.existsByDesCorreo(request.correo())) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }

        CatRol rolAlumno = rolRepository.findByCodRol("ROL_ALUMNO")
                .orElseThrow(() -> new RolNoEncontradoException("ROL_ALUMNO"));

        String contrasenaTemp = guardarAlumno(
                request.nombres(),
                request.apellidos(),
                request.correo(),
                rolAlumno
        );

        SegUsuario usuario = usuarioRepository.findByDesCorreo(request.correo())
                .orElseThrow();

        SegUsuario admin = adminAutenticado();
        registrarLogAdmin(admin, usuario,
                "CREAR_USUARIO",
                "Usuario creado con contraseña temporal");

        return new CrearUsuarioResponse(
                toResponse(usuario),
                contrasenaTemp
        );
    }

    // ── CUS-07: CARGA MASIVA ──────────────────────────────────────────────────

    @Transactional
    public CargaMasivaResponse cargarMasiva(MultipartFile archivo) {
        String filename = archivo.getOriginalFilename() != null
                ? archivo.getOriginalFilename().toLowerCase() : "";

        if (filename.endsWith(".xlsx")) {
            return procesarExcel(archivo);
        } else if (filename.endsWith(".csv")) {
            return procesarCsv(archivo);
        } else {
            throw new IllegalArgumentException(
                    "Formato de archivo no soportado. Por favor, suba un archivo .csv o .xlsx");
        }
    }

    // ── Procesamiento Excel ───────────────────────────────────────────────────

    private CargaMasivaResponse procesarExcel(MultipartFile archivo) {
        List<ErrorFila> errores = new ArrayList<>();
        List<CredencialAlumno> credenciales = new ArrayList<>();
        int exitosos = 0;
        int fila = 2; // fila 1 = cabecera

        CatRol rolAlumno = rolRepository.findByCodRol("ROL_ALUMNO")
                .orElseThrow(() -> new RolNoEncontradoException("ROL_ALUMNO"));

        try (Workbook workbook = new XSSFWorkbook(archivo.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);

            // Validar cabeceras mínimas esperadas: Nombres | Apellidos | Correo
            Row cabecera = sheet.getRow(0);
            if (!validarCabeceraExcel(cabecera)) {
                throw new IllegalArgumentException(
                        "El archivo no tiene el formato correcto. " +
                        "Columnas requeridas: Nombres, Apellidos, Correo");
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++, fila++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;

                String nombres   = getCeldaString(row, 0);
                String apellidos = getCeldaString(row, 1);
                String correo    = getCeldaString(row, 2);
                String errorMsg  = validarFila(nombres, apellidos, correo);

                if (errorMsg != null) {
                    errores.add(new ErrorFila(fila, correo, errorMsg));
                    continue;
                }

                if (usuarioRepository.existsByDesCorreo(correo)) {
                    errores.add(new ErrorFila(fila, correo, "El correo ya está registrado."));
                    continue;
                }

                String contrasenaTemp = guardarAlumno(nombres, apellidos, correo, rolAlumno);
                credenciales.add(new CredencialAlumno(correo, nombres, apellidos, contrasenaTemp));
                exitosos++;
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error procesando Excel: ", e);
            throw new RuntimeException("Error al procesar el archivo Excel.");
        }

        registrarLogCargaMasiva(exitosos, errores.size());
        return new CargaMasivaResponse(
                exitosos + errores.size(),
                exitosos,
                errores.size(),
                credenciales,
                errores
        );
    }

    // ── Procesamiento CSV ─────────────────────────────────────────────────────

    private CargaMasivaResponse procesarCsv(MultipartFile archivo) {
        List<ErrorFila> errores = new ArrayList<>();
        List<CredencialAlumno> credenciales = new ArrayList<>();
        int exitosos = 0;
        int fila = 2;

        CatRol rolAlumno = rolRepository.findByCodRol("ROL_ALUMNO")
                .orElseThrow(() -> new RolNoEncontradoException("ROL_ALUMNO"));

        try (CSVReader reader = new CSVReader(
                new InputStreamReader(archivo.getInputStream(), StandardCharsets.UTF_8))) {

            String[] cabecera = reader.readNext();
            if (!validarCabeceraCSV(cabecera)) {
                throw new IllegalArgumentException(
                        "El archivo no tiene el formato correcto. " +
                        "Columnas requeridas: Nombres, Apellidos, Correo");
            }

            String[] linea;
            while ((linea = reader.readNext()) != null) {
                if (linea.length < 3) {
                    errores.add(new ErrorFila(fila, "", "Fila incompleta."));
                    fila++;
                    continue;
                }

                String nombres   = linea[0].trim();
                String apellidos = linea[1].trim();
                String correo    = linea[2].trim();
                String errorMsg  = validarFila(nombres, apellidos, correo);

                if (errorMsg != null) {
                    errores.add(new ErrorFila(fila, correo, errorMsg));
                    fila++;
                    continue;
                }

                if (usuarioRepository.existsByDesCorreo(correo)) {
                    errores.add(new ErrorFila(fila, correo, "El correo ya está registrado."));
                    fila++;
                    continue;
                }

                String contrasenaTemp = guardarAlumno(nombres, apellidos, correo, rolAlumno);
                credenciales.add(new CredencialAlumno(correo, nombres, apellidos, contrasenaTemp));
                exitosos++;
                fila++;
            }

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error procesando CSV: ", e);
            throw new RuntimeException("Error al procesar el archivo CSV.");
        }

        registrarLogCargaMasiva(exitosos, errores.size());
        return new CargaMasivaResponse(
                exitosos + errores.size(),
                exitosos,
                errores.size(),
                credenciales,
                errores
        );
    }

    // ── Helpers privados ──────────────────────────────────────────────────────

    private String guardarAlumno(String nombres, String apellidos, String correo, CatRol rol) {
        String contrasena = generarContrasenaAleatoria();
        SegUsuario alumno = SegUsuario.builder()
                .rol(rol)
                .desNombres(nombres)
                .desApellidos(apellidos)
                .desCorreo(correo)
                .pwdContrasena(passwordEncoder.encode(contrasena))
                .estActivo(true)
                .estPwdTemporal(true)
                .build();
        usuarioRepository.save(alumno);
        log.debug("Alumno registrado (carga masiva) → {}", correo);
        return contrasena;
    }

    private String validarFila(String nombres, String apellidos, String correo) {
        if (nombres == null || nombres.isBlank())
            return "El campo Nombres está vacío.";
        if (apellidos == null || apellidos.isBlank())
            return "El campo Apellidos está vacío.";
        if (correo == null || correo.isBlank())
            return "El campo Correo está vacío.";
        if (!correo.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]+$"))
            return "Formato de correo inválido.";
        return null;
    }

    private boolean validarCabeceraExcel(Row cabecera) {
        if (cabecera == null) return false;
        return getCeldaString(cabecera, 0).equalsIgnoreCase("nombres")
            && getCeldaString(cabecera, 1).equalsIgnoreCase("apellidos")
            && getCeldaString(cabecera, 2).equalsIgnoreCase("correo");
    }

    private boolean validarCabeceraCSV(String[] cabecera) {
        if (cabecera == null || cabecera.length < 3) return false;
        return cabecera[0].trim().equalsIgnoreCase("nombres")
            && cabecera[1].trim().equalsIgnoreCase("apellidos")
            && cabecera[2].trim().equalsIgnoreCase("correo");
    }

    private String getCeldaString(Row row, int col) {
        Cell cell = row.getCell(col);
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING  -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue()).trim();
            default      -> "";
        };
    }

    private UsuarioResponse toResponse(SegUsuario u) {
        return new UsuarioResponse(
                u.getIdUsuario(),
                u.getDesNombres(),
                u.getDesApellidos(),
                u.getDesCorreo(),
                u.getRol().getCodRol(),
                u.getEstActivo(),
                u.getEstPwdTemporal(),
                u.getFecCreacion(),
                u.getFecUltimoAcceso()
        );
    }

    private SegUsuario adminAutenticado() {
        return (SegUsuario) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    private String generarContrasenaAleatoria() {
        byte[] bytes = new byte[9];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private void registrarLogAdmin(SegUsuario admin, SegUsuario afectado,
                                    String accion, String detalle) {
        logAdminRepository.save(AudLogAdmin.builder()
                .administrador(admin)
                .usuarioAfectado(afectado)
                .desAccion(accion)
                .desDetalle(detalle)
                .build());
    }

    private void registrarLogCargaMasiva(int exitosos, int fallidos) {
        SegUsuario admin = adminAutenticado();
        logAdminRepository.save(AudLogAdmin.builder()
                .administrador(admin)
                .desAccion("CARGA_MASIVA")
                .desDetalle("Carga masiva: " + exitosos + " exitosos, " + fallidos + " fallidos.")
                .build());
    }
}