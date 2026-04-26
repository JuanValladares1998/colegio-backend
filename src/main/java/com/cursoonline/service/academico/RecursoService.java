package com.cursoonline.service.academico;

import com.cursoonline.dto.academico.request.RecursoRequest;
import com.cursoonline.dto.academico.response.RecursoResponse;
import com.cursoonline.entity.academico.CatTipoRecurso;
import com.cursoonline.entity.academico.TraLeccion;
import com.cursoonline.entity.academico.TraRecurso;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.exception.academico.*;
import com.cursoonline.repository.academico.*;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecursoService {

    private final TraRecursoRepository           recursoRepository;
    private final TraLeccionRepository           leccionRepository;
    private final CatTipoRecursoRepository       tipoRecursoRepository;
    private final RelProfesorSeccionRepository   profesorSeccionRepository;
    private final RelAlumnoSeccionRepository     alumnoSeccionRepository;
    private final FileStorageService             fileStorageService;

    private static final String COD_ARCHIVO = "ARCHIVO";
    private static final String COD_ENLACE  = "ENLACE";
    private static final String COD_VIDEO   = "VIDEO";

    // ── LECTURA ───────────────────────────────────────────────────────────────

    public List<RecursoResponse> listarPorLeccion(Integer idLeccion, SegUsuario usuario) {
        TraLeccion leccion = leccionRepository.findById(idLeccion)
                .orElseThrow(() -> new LeccionNoEncontradaException(idLeccion));

        Integer idCurso = leccion.getModulo().getCurso().getIdCurso();
        validarAccesoLectura(usuario, idCurso);

        // Si es alumno y la lección no está publicada → 404 (igual que en LeccionService)
        if ("ROL_ALUMNO".equals(usuario.getRol().getCodRol())
                && !Boolean.TRUE.equals(leccion.getEstPublicada())) {
            throw new LeccionNoEncontradaException(idLeccion);
        }

        return recursoRepository
                .findByLeccion_IdLeccionAndEstActivoTrueOrderByIdRecurso(idLeccion)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ── ESCRITURA ─────────────────────────────────────────────────────────────

    @Transactional
    public RecursoResponse crear(RecursoRequest request, SegUsuario usuario) {
        TraLeccion leccion = leccionRepository.findById(request.idLeccion())
                .orElseThrow(() -> new LeccionNoEncontradaException(request.idLeccion()));

        validarAccesoEscritura(usuario, leccion.getModulo().getCurso().getIdCurso());

        CatTipoRecurso tipo = tipoRecursoRepository.findById(request.idTipoRecurso())
                .orElseThrow(() -> new TipoRecursoNoEncontradoException(request.idTipoRecurso()));

        // Validación según tipo
        String urlRuta = validarYResolverUrl(tipo, request.urlRuta());

        TraRecurso recurso = TraRecurso.builder()
                .leccion(leccion)
                .tipoRecurso(tipo)
                .desNombre(request.desNombre())
                .urlRuta(urlRuta)
                .estActivo(true)
                .build();

        recursoRepository.save(recurso);
        log.info("Recurso creado → {} ({}) en lección {}",
                recurso.getDesNombre(), tipo.getCodTipo(), leccion.getDesNombre());
        return toResponse(recurso);
    }

    @Transactional
    public void eliminar(Integer id, SegUsuario usuario) {
        TraRecurso recurso = recursoRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException(id));

        Integer idCurso = recurso.getLeccion().getModulo().getCurso().getIdCurso();
        validarAccesoEscritura(usuario, idCurso);

        // Si es ARCHIVO, eliminar también el archivo físico
        if (COD_ARCHIVO.equals(recurso.getTipoRecurso().getCodTipo())
                && recurso.getUrlRuta() != null) {
            fileStorageService.eliminar(recurso.getUrlRuta());
        }

        recurso.setEstActivo(false);
        recursoRepository.save(recurso);
        log.info("Recurso eliminado → {}", recurso.getDesNombre());
    }

    // ── Validaciones por tipo ─────────────────────────────────────────────────

    /**
     * - ENLACE / VIDEO: la URL es obligatoria y debe empezar con http:// o https://
     * - ARCHIVO: en este sub-bloque NO se permite crear directamente desde JSON;
     *   se manejará en el 3.4 con un endpoint multipart distinto.
     */
    private String validarYResolverUrl(CatTipoRecurso tipo, String urlRequest) {
        String cod = tipo.getCodTipo();

        if (COD_ARCHIVO.equals(cod)) {
            throw new RecursoInvalidoException(
                "Para crear un recurso de tipo ARCHIVO usa el endpoint de subida de archivo.");
        }

        if (COD_ENLACE.equals(cod) || COD_VIDEO.equals(cod)) {
            if (urlRequest == null || urlRequest.isBlank()) {
                throw new RecursoInvalidoException(
                    "La URL es obligatoria para recursos de tipo " + cod + ".");
            }
            String url = urlRequest.trim();
            if (!url.startsWith("http://") && !url.startsWith("https://")) {
                throw new RecursoInvalidoException(
                    "La URL debe empezar con http:// o https://.");
            }
            return url;
        }

        throw new RecursoInvalidoException("Tipo de recurso desconocido: " + cod);
    }

    // ── Validaciones de acceso (mismo patrón que LeccionService) ──────────────

    private void validarAccesoEscritura(SegUsuario usuario, Integer idCurso) {
        String rol = usuario.getRol().getCodRol();

        if ("ROL_ADMIN".equals(rol)) return;

        if ("ROL_PROFESOR".equals(rol)) {
            if (!profesorSeccionRepository.profesorTieneAccesoACurso(
                    usuario.getIdUsuario(), idCurso)) {
                throw new AccesoCursoDenegadoException();
            }
            return;
        }

        throw new AccesoCursoDenegadoException();
    }

    private void validarAccesoLectura(SegUsuario usuario, Integer idCurso) {
        String rol = usuario.getRol().getCodRol();

        if ("ROL_ADMIN".equals(rol)) return;

        if ("ROL_PROFESOR".equals(rol)) {
            if (!profesorSeccionRepository.profesorTieneAccesoACurso(
                    usuario.getIdUsuario(), idCurso)) {
                throw new AccesoCursoDenegadoException();
            }
            return;
        }

        if ("ROL_ALUMNO".equals(rol)) {
            if (!alumnoSeccionRepository.alumnoTieneAccesoACurso(
                    usuario.getIdUsuario(), idCurso)) {
                throw new AccesoCursoDenegadoException();
            }
            return;
        }

        throw new AccesoCursoDenegadoException();
    }

    private RecursoResponse toResponse(TraRecurso r) {
        return new RecursoResponse(
                r.getIdRecurso(),
                r.getLeccion() != null ? r.getLeccion().getIdLeccion() : null,
                r.getTipoRecurso() != null ? r.getTipoRecurso().getIdTipoRecurso() : null,
                r.getTipoRecurso() != null ? r.getTipoRecurso().getCodTipo() : null,
                r.getTipoRecurso() != null ? r.getTipoRecurso().getDesNombre() : null,
                r.getDesNombre(),
                r.getUrlRuta(),
                r.getEstActivo(),
                r.getFecCreacion()
        );
    }

    @Transactional
public RecursoResponse crearConArchivo(Integer idLeccion,
                                       String desNombre,
                                       MultipartFile archivo,
                                       SegUsuario usuario) {
    TraLeccion leccion = leccionRepository.findById(idLeccion)
            .orElseThrow(() -> new LeccionNoEncontradaException(idLeccion));

    validarAccesoEscritura(usuario, leccion.getModulo().getCurso().getIdCurso());

    if (desNombre == null || desNombre.isBlank()) {
        throw new RecursoInvalidoException("El nombre del recurso es obligatorio.");
    }

    CatTipoRecurso tipo = tipoRecursoRepository.findByCodTipo(COD_ARCHIVO)
            .orElseThrow(() -> new RecursoInvalidoException(
                "Tipo de recurso ARCHIVO no está configurado."));

    String rutaRelativa = fileStorageService.guardar(archivo);

    TraRecurso recurso = TraRecurso.builder()
            .leccion(leccion)
            .tipoRecurso(tipo)
            .desNombre(desNombre.trim())
            .urlRuta(rutaRelativa)
            .estActivo(true)
            .build();

    recursoRepository.save(recurso);
    log.info("Recurso ARCHIVO creado → {} (ruta: {})", recurso.getDesNombre(), rutaRelativa);
    return toResponse(recurso);
}
public ArchivoDescarga obtenerArchivoParaDescarga(Integer idRecurso, SegUsuario usuario) {
    TraRecurso recurso = recursoRepository.findById(idRecurso)
            .orElseThrow(() -> new RecursoNoEncontradoException(idRecurso));

    if (!Boolean.TRUE.equals(recurso.getEstActivo())) {
        throw new RecursoNoEncontradoException(idRecurso);
    }

    if (!COD_ARCHIVO.equals(recurso.getTipoRecurso().getCodTipo())) {
        throw new RecursoInvalidoException(
            "Solo los recursos de tipo ARCHIVO se pueden descargar. " +
            "Los de tipo " + recurso.getTipoRecurso().getCodTipo() +
            " se acceden por su URL.");
    }

    Integer idCurso = recurso.getLeccion().getModulo().getCurso().getIdCurso();
    validarAccesoLectura(usuario, idCurso);

    // Si es alumno, además la lección debe estar publicada
    if ("ROL_ALUMNO".equals(usuario.getRol().getCodRol())
            && !Boolean.TRUE.equals(recurso.getLeccion().getEstPublicada())) {
        throw new RecursoNoEncontradoException(idRecurso);
    }

    Path rutaArchivo = fileStorageService.resolverRuta(recurso.getUrlRuta());
    return new ArchivoDescarga(rutaArchivo, recurso.getDesNombre());
}

/** DTO interno solo para devolver datos al controller en la descarga. */
public record ArchivoDescarga(Path ruta, String nombreOriginal) {}
}