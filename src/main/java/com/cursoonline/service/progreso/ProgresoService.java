package com.cursoonline.service.progreso;

import com.cursoonline.dto.progreso.EstadoModulo;
import com.cursoonline.dto.progreso.response.ModuloProgresoResponse;
import com.cursoonline.dto.progreso.response.ProgresoCursoResponse;
import com.cursoonline.dto.progreso.response.ProgresoLeccionResponse;
import com.cursoonline.entity.academico.CatCurso;
import com.cursoonline.entity.academico.TraLeccion;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.entity.progreso.TraProgresoLeccion;
import com.cursoonline.exception.academico.AccesoCursoDenegadoException;
import com.cursoonline.exception.academico.LeccionNoEncontradaException;
import com.cursoonline.repository.academico.RelAlumnoSeccionRepository;
import com.cursoonline.repository.academico.TraLeccionRepository;
import com.cursoonline.repository.progreso.TraProgresoLeccionRepository;
import com.cursoonline.repository.progreso.TraProgresoLeccionRepository.ModuloStatsView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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