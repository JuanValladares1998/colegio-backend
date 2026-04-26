package com.cursoonline.service.academico;

import com.cursoonline.dto.academico.request.AsignarProfesorRequest;
import com.cursoonline.dto.academico.request.SeccionRequest;
import com.cursoonline.dto.academico.response.InscripcionAlumnoResponse;
import com.cursoonline.dto.academico.response.SeccionResponse;
import com.cursoonline.entity.academico.CatAnioEscolar;
import com.cursoonline.entity.academico.CatCurso;
import com.cursoonline.entity.academico.RelAlumnoSeccion;
import com.cursoonline.entity.academico.RelProfesorSeccion;
import com.cursoonline.entity.academico.TraSeccion;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.exception.academico.*;
import com.cursoonline.exception.usuario.UsuarioNoEncontradoException;
import com.cursoonline.repository.academico.*;
import com.cursoonline.repository.auth.SegUsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import com.cursoonline.dto.academico.request.InscribirAlumnoRequest;
import com.cursoonline.exception.academico.AlumnoYaInscritoException;
import com.cursoonline.exception.academico.InscripcionNoEncontradaException;
import com.cursoonline.exception.academico.UsuarioNoEsAlumnoException;
import com.cursoonline.repository.academico.RelAlumnoSeccionRepository;
@Slf4j
@Service
@RequiredArgsConstructor
public class SeccionService {

    private final TraSeccionRepository       seccionRepository;
    private final CatCursoRepository         cursoRepository;
    private final CatAnioEscolarRepository   anioEscolarRepository;
    private final RelProfesorSeccionRepository profesorSeccionRepository;
    private final SegUsuarioRepository       usuarioRepository;
    private final RelAlumnoSeccionRepository alumnoSeccionRepository;

    // ── CUS-08: LISTAR ────────────────────────────────────────────────────────

    public Page<SeccionResponse> listar(Pageable pageable) {
        return seccionRepository.findByEstActivaTrue(pageable)
                .map(this::toResponse);
    }

    public Page<SeccionResponse> listarPorCurso(Integer idCurso, Pageable pageable) {
        return seccionRepository.findByCurso_IdCursoAndEstActivaTrue(idCurso, pageable)
                .map(this::toResponse);
    }

    public Page<SeccionResponse> listarPorAnio(Integer idAnio, Pageable pageable) {
        return seccionRepository.findByAnioEscolar_IdAnioEscolarAndEstActivaTrue(idAnio, pageable)
                .map(this::toResponse);
    }

    public SeccionResponse obtener(Integer id) {
        return toResponse(
                seccionRepository.findById(id)
                        .orElseThrow(() -> new SeccionNoEncontradaException(id))
        );
    }

    // ── CUS-08: CREAR ─────────────────────────────────────────────────────────

    @Transactional
    public SeccionResponse crear(SeccionRequest request) {
        CatCurso curso = cursoRepository.findById(request.idCurso())
                .orElseThrow(() -> new CursoNoEncontradoException(request.idCurso()));

        CatAnioEscolar anio = anioEscolarRepository.findById(request.idAnioEscolar())
                .orElseThrow(() -> new AnioEscolarNoEncontradoException(request.idAnioEscolar()));

        if (seccionRepository.existsByDesNombreAndCurso_IdCursoAndAnioEscolar_IdAnioEscolar(
                request.desNombre(), request.idCurso(), request.idAnioEscolar()))
            throw new SeccionYaExisteException(request.desNombre());

        TraSeccion seccion = TraSeccion.builder()
                .curso(curso)
                .anioEscolar(anio)
                .desNombre(request.desNombre())
                .estActiva(true)
                .build();

        seccionRepository.save(seccion);
        log.info("Sección creada → {}", seccion.getDesNombre());
        return toResponse(seccion);
    }

    // ── CUS-08: ACTUALIZAR ────────────────────────────────────────────────────

    @Transactional
    public SeccionResponse actualizar(Integer id, SeccionRequest request) {
        TraSeccion seccion = seccionRepository.findById(id)
                .orElseThrow(() -> new SeccionNoEncontradaException(id));

        CatCurso curso = cursoRepository.findById(request.idCurso())
                .orElseThrow(() -> new CursoNoEncontradoException(request.idCurso()));

        CatAnioEscolar anio = anioEscolarRepository.findById(request.idAnioEscolar())
                .orElseThrow(() -> new AnioEscolarNoEncontradoException(request.idAnioEscolar()));

        seccion.setCurso(curso);
        seccion.setAnioEscolar(anio);
        seccion.setDesNombre(request.desNombre());
        seccionRepository.save(seccion);
        log.info("Sección actualizada → {}", seccion.getDesNombre());
        return toResponse(seccion);
    }

    // ── CUS-09: ASIGNAR PROFESOR ──────────────────────────────────────────────

    @Transactional
    public SeccionResponse asignarProfesor(Integer idSeccion, AsignarProfesorRequest request) {
        TraSeccion seccion = seccionRepository.findById(idSeccion)
                .orElseThrow(() -> new SeccionNoEncontradaException(idSeccion));

        SegUsuario profesor = usuarioRepository.findById(request.idProfesor())
                .orElseThrow(() -> new com.cursoonline.exception.usuario.UsuarioNoEncontradoException(
                        request.idProfesor()));

        // Validar que sea profesor
        if (!profesor.getRol().getCodRol().equals("ROL_PROFESOR"))
            throw new UsuarioNoEsProfesorException();

        // Validar que la sección no tenga ya un profesor
        if (profesorSeccionRepository
                .findBySeccion_IdSeccionAndEstActivoTrue(idSeccion).isPresent())
            throw new ProfesorYaAsignadoException();

        RelProfesorSeccion asignacion = RelProfesorSeccion.builder()
                .profesor(profesor)
                .seccion(seccion)
                .estActivo(true)
                .build();

        profesorSeccionRepository.save(asignacion);
        log.info("Profesor {} asignado a sección {}", profesor.getDesCorreo(), seccion.getDesNombre());
        return toResponse(seccion);
    }

    // ── CUS-09: REMOVER PROFESOR ──────────────────────────────────────────────

    @Transactional
    public SeccionResponse removerProfesor(Integer idSeccion) {
        TraSeccion seccion = seccionRepository.findById(idSeccion)
                .orElseThrow(() -> new SeccionNoEncontradaException(idSeccion));

        RelProfesorSeccion asignacion = profesorSeccionRepository
                .findBySeccion_IdSeccionAndEstActivoTrue(idSeccion)
                .orElseThrow(() -> new IllegalStateException(
                        "Esta sección no tiene un profesor asignado."));

        asignacion.setEstActivo(false);
        profesorSeccionRepository.save(asignacion);
        log.info("Profesor removido de sección {}", seccion.getDesNombre());
        return toResponse(seccion);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private SeccionResponse toResponse(TraSeccion s) {
        Optional<RelProfesorSeccion> asignacion = profesorSeccionRepository
                .findBySeccion_IdSeccionAndEstActivoTrue(s.getIdSeccion());

        Integer idProfesor  = asignacion.map(a -> a.getProfesor().getIdUsuario()).orElse(null);
        String  desProfesor = asignacion.map(a ->
                a.getProfesor().getDesNombres() + " " + a.getProfesor().getDesApellidos()
        ).orElse(null);

        return new SeccionResponse(
                s.getIdSeccion(),
                s.getCurso().getIdCurso(),
                s.getCurso().getDesNombre(),
                s.getAnioEscolar().getIdAnioEscolar(),
                s.getAnioEscolar().getValAnio(),
                s.getDesNombre(),
                s.getEstActiva(),
                s.getFecCreacion(),
                idProfesor,
                desProfesor
        );
    }
    // ── Gestión de alumnos en la sección (matrícula) ──────────────────────────

@Transactional
public InscripcionAlumnoResponse inscribirAlumno(Integer idSeccion,
                                                 InscribirAlumnoRequest request) {

    TraSeccion seccion = seccionRepository.findById(idSeccion)
            .orElseThrow(() -> new SeccionNoEncontradaException(idSeccion));

    SegUsuario alumno = usuarioRepository.findById(request.idUsuario())
            .orElseThrow(() -> new UsuarioNoEncontradoException(request.idUsuario()));

    // El usuario debe tener rol ALUMNO
    if (!"ROL_ALUMNO".equals(alumno.getRol().getCodRol())) {
        throw new UsuarioNoEsAlumnoException(alumno.getIdUsuario());
    }

    // Duplicado en la misma sección
    if (alumnoSeccionRepository
            .findByAlumno_IdUsuarioAndSeccion_IdSeccionAndEstActivoTrue(
                    alumno.getIdUsuario(), idSeccion)
            .isPresent()) {
        throw AlumnoYaInscritoException.enMismaSeccion();
    }

    // Regla: un alumno = una sección por año escolar
    Integer idAnio = seccion.getAnioEscolar().getIdAnioEscolar();
    if (alumnoSeccionRepository.alumnoTieneInscripcionEnAnio(
            alumno.getIdUsuario(), idAnio)) {
        throw AlumnoYaInscritoException.enOtraSeccionDelAnio();
    }

    RelAlumnoSeccion inscripcion = RelAlumnoSeccion.builder()
            .alumno(alumno)
            .seccion(seccion)
            .estActivo(true)
            .build();

    alumnoSeccionRepository.save(inscripcion);
    log.info("Alumno {} inscrito en sección {}",
            alumno.getDesCorreo(), seccion.getDesNombre());

    return toInscripcionResponse(inscripcion);
}

@Transactional
public void darDeBajaAlumno(Integer idSeccion, Integer idUsuario) {

    RelAlumnoSeccion inscripcion = alumnoSeccionRepository
            .findByAlumno_IdUsuarioAndSeccion_IdSeccionAndEstActivoTrue(
                    idUsuario, idSeccion)
            .orElseThrow(() -> new InscripcionNoEncontradaException(idUsuario, idSeccion));

    inscripcion.setEstActivo(false);
    alumnoSeccionRepository.save(inscripcion);
    log.info("Alumno {} dado de baja de sección {}", idUsuario, idSeccion);
}

public List<InscripcionAlumnoResponse> listarAlumnosDeSeccion(Integer idSeccion) {
    if (!seccionRepository.existsById(idSeccion)) {
        throw new SeccionNoEncontradaException(idSeccion);
    }

    return alumnoSeccionRepository
            .findBySeccion_IdSeccionAndEstActivoTrueOrderByAlumno_DesApellidos(idSeccion)
            .stream()
            .map(this::toInscripcionResponse)
            .toList();
}

// ── Mapper privado ────────────────────────────────────────────────────────

private InscripcionAlumnoResponse toInscripcionResponse(RelAlumnoSeccion ras) {
    SegUsuario a = ras.getAlumno();
    TraSeccion s = ras.getSeccion();
    return new InscripcionAlumnoResponse(
            ras.getIdAlumnoSeccion(),
            a.getIdUsuario(),
            a.getDesNombres(),
            a.getDesApellidos(),
            a.getDesCorreo(),
            s.getIdSeccion(),
            s.getDesNombre(),
            ras.getEstActivo(),
            ras.getFecInscripcion()
    );
}
}