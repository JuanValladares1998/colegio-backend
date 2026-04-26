package com.cursoonline.service.academico;

import com.cursoonline.dto.academico.request.LeccionRequest;
import com.cursoonline.dto.academico.response.LeccionResponse;
import com.cursoonline.entity.academico.TraLeccion;
import com.cursoonline.entity.academico.TraModulo;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.exception.academico.AccesoCursoDenegadoException;
import com.cursoonline.exception.academico.LeccionNoEncontradaException;
import com.cursoonline.exception.academico.LeccionYaExisteException;
import com.cursoonline.exception.academico.ModuloNoEncontradoException;
import com.cursoonline.repository.academico.RelAlumnoSeccionRepository;
import com.cursoonline.repository.academico.RelProfesorSeccionRepository;
import com.cursoonline.repository.academico.TraLeccionRepository;
import com.cursoonline.repository.academico.TraModuloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeccionService {

    private final TraLeccionRepository           leccionRepository;
    private final TraModuloRepository            moduloRepository;
    private final RelProfesorSeccionRepository   profesorSeccionRepository;
    private final RelAlumnoSeccionRepository     alumnoSeccionRepository;

    // ── LISTADO POR MÓDULO ────────────────────────────────────────────────────

    public List<LeccionResponse> listarPorModulo(Integer idModulo, SegUsuario usuario) {
        TraModulo modulo = moduloRepository.findById(idModulo)
                .orElseThrow(() -> new ModuloNoEncontradoException(idModulo));

        validarAccesoLectura(usuario, modulo.getCurso().getIdCurso());

        // Alumno ve solo publicadas; admin/profesor ven todas
        boolean esAlumno = "ROL_ALUMNO".equals(usuario.getRol().getCodRol());
        List<TraLeccion> lecciones = esAlumno
                ? leccionRepository.findByModulo_IdModuloAndEstActivaTrueAndEstPublicadaTrueOrderByValOrden(idModulo)
                : leccionRepository.findByModulo_IdModuloAndEstActivaTrueOrderByValOrden(idModulo);

        return lecciones.stream().map(this::toResponse).toList();
    }

    public LeccionResponse obtener(Integer id, SegUsuario usuario) {
        TraLeccion leccion = leccionRepository.findById(id)
                .orElseThrow(() -> new LeccionNoEncontradaException(id));

        Integer idCurso = leccion.getModulo().getCurso().getIdCurso();
        validarAccesoLectura(usuario, idCurso);

        // Si es alumno, además debe estar publicada
        if ("ROL_ALUMNO".equals(usuario.getRol().getCodRol())
                && !Boolean.TRUE.equals(leccion.getEstPublicada())) {
            throw new LeccionNoEncontradaException(id);
        }

        return toResponse(leccion);
    }

    // ── ESCRITURA (admin / profesor) ──────────────────────────────────────────

    @Transactional
    public LeccionResponse crear(LeccionRequest request, SegUsuario usuario) {
        TraModulo modulo = moduloRepository.findById(request.idModulo())
                .orElseThrow(() -> new ModuloNoEncontradoException(request.idModulo()));

        validarAccesoEscritura(usuario, modulo.getCurso().getIdCurso());

        if (leccionRepository.existsByModulo_IdModuloAndDesNombreAndEstActivaTrue(
                request.idModulo(), request.desNombre())) {
            throw new LeccionYaExisteException(request.desNombre());
        }

        TraLeccion leccion = TraLeccion.builder()
                .modulo(modulo)
                .desNombre(request.desNombre())
                .desContenido(request.desContenido())
                .valOrden(request.valOrden())
                .estObligatoria(request.estObligatoria())
                .estPublicada(false)
                .estActiva(true)
                .build();

        leccionRepository.save(leccion);
        log.info("Lección creada → {} (módulo: {})", leccion.getDesNombre(), modulo.getDesNombre());
        return toResponse(leccion);
    }

    @Transactional
    public LeccionResponse actualizar(Integer id, LeccionRequest request, SegUsuario usuario) {
        TraLeccion leccion = leccionRepository.findById(id)
                .orElseThrow(() -> new LeccionNoEncontradaException(id));

        validarAccesoEscritura(usuario, leccion.getModulo().getCurso().getIdCurso());

        leccion.setDesNombre(request.desNombre());
        leccion.setDesContenido(request.desContenido());
        leccion.setValOrden(request.valOrden());
        leccion.setEstObligatoria(request.estObligatoria());

        leccionRepository.save(leccion);
        log.info("Lección actualizada → {}", leccion.getDesNombre());
        return toResponse(leccion);
    }

    @Transactional
    public LeccionResponse publicar(Integer id, SegUsuario usuario) {
        TraLeccion leccion = leccionRepository.findById(id)
                .orElseThrow(() -> new LeccionNoEncontradaException(id));

        validarAccesoEscritura(usuario, leccion.getModulo().getCurso().getIdCurso());

        leccion.setEstPublicada(true);
        leccion.setFecPublicacion(LocalDateTime.now());
        leccionRepository.save(leccion);
        log.info("Lección publicada → {}", leccion.getDesNombre());
        return toResponse(leccion);
    }

    @Transactional
    public LeccionResponse despublicar(Integer id, SegUsuario usuario) {
        TraLeccion leccion = leccionRepository.findById(id)
                .orElseThrow(() -> new LeccionNoEncontradaException(id));

        validarAccesoEscritura(usuario, leccion.getModulo().getCurso().getIdCurso());

        leccion.setEstPublicada(false);
        leccion.setFecPublicacion(null);
        leccionRepository.save(leccion);
        log.info("Lección despublicada → {}", leccion.getDesNombre());
        return toResponse(leccion);
    }

    @Transactional
    public void eliminar(Integer id, SegUsuario usuario) {
        TraLeccion leccion = leccionRepository.findById(id)
                .orElseThrow(() -> new LeccionNoEncontradaException(id));

        validarAccesoEscritura(usuario, leccion.getModulo().getCurso().getIdCurso());

        leccion.setEstActiva(false);
        leccionRepository.save(leccion);
        log.info("Lección eliminada lógicamente → {}", leccion.getDesNombre());
    }

    // ── Validaciones de acceso ────────────────────────────────────────────────

    /** Escritura: admin libre, profesor solo si tiene sección de ese curso. */
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

    /** Lectura: admin libre; profesor solo sus cursos; alumno solo cursos inscritos. */
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

    private LeccionResponse toResponse(TraLeccion l) {
        return new LeccionResponse(
                l.getIdLeccion(),
                l.getModulo() != null ? l.getModulo().getIdModulo() : null,
                l.getModulo() != null ? l.getModulo().getDesNombre() : null,
                l.getDesNombre(),
                l.getDesContenido(),
                l.getValOrden(),
                l.getEstObligatoria(),
                l.getEstPublicada(),
                l.getEstActiva(),
                l.getFecCreacion(),
                l.getFecPublicacion()
        );
    }
}