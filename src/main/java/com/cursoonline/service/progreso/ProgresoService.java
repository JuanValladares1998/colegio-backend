package com.cursoonline.service.progreso;

import com.cursoonline.dto.progreso.EstadoModulo;
import com.cursoonline.dto.progreso.response.AlumnoResumenResponse;
import com.cursoonline.dto.progreso.response.DetalleProgresoAlumnoResponse;
import com.cursoonline.dto.progreso.response.FilaTableroResponse;
import com.cursoonline.dto.progreso.response.LeccionEstadoResponse;
import com.cursoonline.dto.progreso.response.ModuloDetalleResponse;
import com.cursoonline.dto.progreso.response.ModuloProgresoResponse;
import com.cursoonline.dto.progreso.response.ProgresoCursoResponse;
import com.cursoonline.dto.progreso.response.ProgresoLeccionResponse;
import com.cursoonline.dto.progreso.response.TableroSeccionResponse;
import com.cursoonline.entity.academico.CatCurso;
import com.cursoonline.entity.academico.TraLeccion;
import com.cursoonline.entity.academico.TraSeccion;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.entity.progreso.TraProgresoLeccion;
import com.cursoonline.exception.academico.AccesoCursoDenegadoException;
import com.cursoonline.exception.academico.CursoNoEncontradoException;
import com.cursoonline.exception.academico.LeccionNoEncontradaException;
import com.cursoonline.exception.academico.SeccionNoEncontradaException;
import com.cursoonline.exception.academico.UsuarioNoEsAlumnoException;
import com.cursoonline.exception.usuario.UsuarioNoEncontradoException;
import com.cursoonline.repository.academico.CatCursoRepository;
import com.cursoonline.repository.academico.RelAlumnoSeccionRepository;
import com.cursoonline.repository.academico.RelProfesorSeccionRepository;
import com.cursoonline.repository.academico.TraLeccionRepository;
import com.cursoonline.repository.academico.TraSeccionRepository;
import com.cursoonline.repository.auth.SegUsuarioRepository;
import com.cursoonline.repository.progreso.TraProgresoLeccionRepository;
import com.cursoonline.repository.progreso.TraProgresoLeccionRepository.FilaTableroView;
import com.cursoonline.repository.progreso.TraProgresoLeccionRepository.HistorialLeccionView;
import com.cursoonline.repository.progreso.TraProgresoLeccionRepository.ModuloStatsView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;


import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProgresoService {

    private final TraProgresoLeccionRepository  progresoRepository;
    private final TraLeccionRepository          leccionRepository;
    private final RelAlumnoSeccionRepository    alumnoSeccionRepository;
    private final RelProfesorSeccionRepository  profesorSeccionRepository;
private final TraSeccionRepository          seccionRepository;
private final SegUsuarioRepository  usuarioRepository;
private final CatCursoRepository    cursoRepository;

    // ── 4.1 Marcar lección completada ─────────────────────────────────────────

    @Transactional
    public ProgresoLeccionResponse marcarLeccionCompletada(
            Integer idLeccion, SegUsuario alumno) {

        TraLeccion leccion = leccionRepository.findById(idLeccion)
                .orElseThrow(() -> new LeccionNoEncontradaException(idLeccion));

        // Lección no publicada o desactivada → 404 (no revelar existencia al alumno)
        if (!Boolean.TRUE.equals(leccion.getEstPublicada())
                || !Boolean.TRUE.equals(leccion.getEstActiva())) {
            throw new LeccionNoEncontradaException(idLeccion);
        }

        Integer idCurso = leccion.getModulo().getCurso().getIdCurso();
        validarAccesoAlumnoACurso(alumno, idCurso);

        Optional<TraProgresoLeccion> existente = progresoRepository
                .findByUsuario_IdUsuarioAndLeccion_IdLeccion(
                        alumno.getIdUsuario(), idLeccion);

        // Idempotencia: ya completada → no se toca BD
        if (existente.isPresent()
                && Boolean.TRUE.equals(existente.get().getEstCompletada())) {
            return toLeccionResponse(existente.get());
        }

        LocalDateTime now = LocalDateTime.now();
        TraProgresoLeccion progreso = existente.orElseGet(() ->
                TraProgresoLeccion.builder()
                        .usuario(alumno)
                        .leccion(leccion)
                        .fecInicio(now)
                        .build()
        );
        progreso.setEstCompletada(true);
        progreso.setFecCompletado(now);

        progresoRepository.save(progreso);
        log.info("Lección completada → alumnoId={}, correo={}, leccion={}",
                alumno.getIdUsuario(), alumno.getDesCorreo(), leccion.getDesNombre());

        return toLeccionResponse(progreso);
    }

    // ── 4.1 Mi progreso (CUS-14) ──────────────────────────────────────────────

    public List<ProgresoCursoResponse> obtenerMiProgreso(SegUsuario alumno) {
        // Reusa el método de M2 (CUS-11) para listar cursos publicados del alumno.
        List<CatCurso> cursos = alumnoSeccionRepository
                .findCursosPublicadosByAlumno(alumno.getIdUsuario(), Pageable.unpaged())
                .getContent();

        return cursos.stream()
                .map(c -> buildProgresoCurso(alumno, c))
                .toList();
    }
    public TableroSeccionResponse obtenerTableroSeccion(
        Integer idSeccion, SegUsuario profesor, Pageable pageable) {

    TraSeccion seccion = seccionRepository.findById(idSeccion)
            .orElseThrow(() -> new SeccionNoEncontradaException(idSeccion));

    Integer idCurso = seccion.getCurso().getIdCurso();
    validarAccesoProfesorACurso(profesor, idCurso);

    Page<FilaTableroView> filas = progresoRepository
            .findTableroPorSeccion(idSeccion, pageable);

    Page<FilaTableroResponse> alumnos = filas.map(this::toFilaResponse);

    long totalObligatorias = filas.getContent().isEmpty()
            ? 0L
            : safeLong(filas.getContent().get(0).getTotalObligatorias());

    return new TableroSeccionResponse(
            seccion.getIdSeccion(),
            seccion.getDesNombre(),
            idCurso,
            seccion.getCurso().getDesNombre(),
            totalObligatorias,
            alumnos
    );
        }

        private void validarAccesoProfesorACurso(SegUsuario profesor, Integer idCurso) {
        boolean tiene = profesorSeccionRepository
                .profesorTieneAccesoACurso(profesor.getIdUsuario(), idCurso);
        if (!tiene) throw new AccesoCursoDenegadoException();
        }

        private FilaTableroResponse toFilaResponse(FilaTableroView v) {
        long total = safeLong(v.getTotalObligatorias());
        long comp  = safeLong(v.getCompletadas());
        double pct = total == 0 ? 0.0
                : Math.round(comp * 10000.0 / total) / 100.0;
        return new FilaTableroResponse(
                v.getIdAlumno(),
                v.getNombres(),
                v.getApellidos(),
                comp,
                total,
                pct,
                v.getUltimaActividad()
        );
        }

    // ── helpers ───────────────────────────────────────────────────────────────

    private ProgresoCursoResponse buildProgresoCurso(SegUsuario alumno, CatCurso curso) {
        List<ModuloStatsView> stats = progresoRepository
                .findModuloStatsPorAlumnoYCurso(alumno.getIdUsuario(), curso.getIdCurso());

        LocalDateTime ultima = progresoRepository
                .findUltimaActividadEnCurso(alumno.getIdUsuario(), curso.getIdCurso());

        long totalCurso = stats.stream().mapToLong(s -> safeLong(s.getTotal())).sum();
        long compCurso  = stats.stream().mapToLong(s -> safeLong(s.getCompletadas())).sum();
        double pct      = totalCurso == 0 ? 0.0
                : Math.round(compCurso * 10000.0 / totalCurso) / 100.0;

        List<ModuloProgresoResponse> modulos = stats.stream()
                .map(s -> new ModuloProgresoResponse(
                        s.getIdModulo(),
                        s.getNombre(),
                        calcularEstado(safeLong(s.getCompletadas()), safeLong(s.getTotal())),
                        safeLong(s.getCompletadas()),
                        safeLong(s.getTotal())
                ))
                .toList();

        return new ProgresoCursoResponse(
                curso.getIdCurso(),
                curso.getDesNombre(),
                totalCurso,
                compCurso,
                pct,
                ultima,
                modulos,
                List.of()  // calificaciones — placeholder M5
        );
    }
    public DetalleProgresoAlumnoResponse obtenerProgresoAlumno(
        Integer idAlumno, Integer idCurso, SegUsuario profesor) {

    // 1. El profesor debe tener acceso al curso
    validarAccesoProfesorACurso(profesor, idCurso);

    // 2. El alumno debe existir y estar inscrito en alguna sección de ese curso
    SegUsuario alumno = usuarioRepository.findById(idAlumno)
            .orElseThrow(() -> new UsuarioNoEncontradoException(idAlumno));

    if (!"ROL_ALUMNO".equals(alumno.getRol().getCodRol())) {
        throw new UsuarioNoEsAlumnoException(idAlumno);
    }

    boolean inscrito = alumnoSeccionRepository
            .alumnoTieneAccesoACurso(idAlumno, idCurso);
    if (!inscrito) throw new AccesoCursoDenegadoException();

    CatCurso curso = cursoRepository.findById(idCurso)
            .orElseThrow(() -> new CursoNoEncontradoException(idCurso));

    // 3. Historial completo de lecciones
    List<HistorialLeccionView> historial = progresoRepository
            .findHistorialPorAlumnoYCurso(idAlumno, idCurso);

    // 4. Agrupar por módulo (LinkedHashMap para conservar el orden de la query)
    Map<Integer, List<HistorialLeccionView>> porModulo = historial.stream()
            .collect(Collectors.groupingBy(
                    HistorialLeccionView::getIdModulo,
                    LinkedHashMap::new,
                    Collectors.toList()));

    List<ModuloDetalleResponse> modulos = porModulo.values().stream()
            .map(this::toModuloDetalle)
            .toList();

    // 5. Totales (solo obligatorias para el porcentaje)
    long total = historial.stream()
            .filter(v -> Boolean.TRUE.equals(v.getObligatoria()))
            .count();
    long comp = historial.stream()
            .filter(v -> Boolean.TRUE.equals(v.getObligatoria())
                      && Boolean.TRUE.equals(v.getCompletada()))
            .count();
    double pct = total == 0 ? 0.0
            : Math.round(comp * 10000.0 / total) / 100.0;

    LocalDateTime ultima = progresoRepository
            .findUltimaActividadEnCurso(idAlumno, idCurso);

    return new DetalleProgresoAlumnoResponse(
            new AlumnoResumenResponse(
                    alumno.getIdUsuario(),
                    alumno.getDesNombres(),
                    alumno.getDesApellidos(),
                    alumno.getDesCorreo()),
            curso.getIdCurso(),
            curso.getDesNombre(),
            total,
            comp,
            pct,
            ultima,
            modulos,
            List.of()  // placeholder M5
    );
}

private ModuloDetalleResponse toModuloDetalle(List<HistorialLeccionView> filas) {
    HistorialLeccionView primera = filas.get(0);
    List<LeccionEstadoResponse> lecciones = filas.stream()
            .map(v -> new LeccionEstadoResponse(
                    v.getIdLeccion(),
                    v.getNombreLeccion(),
                    v.getObligatoria(),
                    Boolean.TRUE.equals(v.getCompletada()),
                    v.getFecCompletado()))
            .toList();
    return new ModuloDetalleResponse(
            primera.getIdModulo(),
            primera.getNombreModulo(),
            lecciones);
}

    private EstadoModulo calcularEstado(long completadas, long total) {
        if (total == 0)            return EstadoModulo.PENDIENTE;
        if (completadas >= total)  return EstadoModulo.COMPLETADO;
        if (completadas > 0)       return EstadoModulo.EN_CURSO;
        return EstadoModulo.PENDIENTE;
    }

    private void validarAccesoAlumnoACurso(SegUsuario alumno, Integer idCurso) {
        boolean tiene = alumnoSeccionRepository
                .alumnoTieneAccesoACurso(alumno.getIdUsuario(), idCurso);
        if (!tiene) throw new AccesoCursoDenegadoException();
    }

    private long safeLong(Long v) { return v == null ? 0L : v; }

    // ── mapper ────────────────────────────────────────────────────────────────

    private ProgresoLeccionResponse toLeccionResponse(TraProgresoLeccion p) {
        return new ProgresoLeccionResponse(
                p.getIdProgreso(),
                p.getLeccion() != null ? p.getLeccion().getIdLeccion() : null,
                p.getLeccion() != null ? p.getLeccion().getDesNombre() : null,
                p.getEstCompletada(),
                p.getFecCompletado()
        );
    }
    
}