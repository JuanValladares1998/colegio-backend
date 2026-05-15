package com.cursoonline.service.evaluacion;

import com.cursoonline.dto.evaluacion.request.EvaluacionRequest;
import com.cursoonline.dto.evaluacion.response.EvaluacionResponse;
import com.cursoonline.entity.academico.TraModulo;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.entity.evaluacion.TraEvaluacion;
import com.cursoonline.exception.academico.AccesoCursoDenegadoException;
import com.cursoonline.exception.academico.ModuloNoEncontradoException;
import com.cursoonline.exception.evaluacion.EvaluacionActivaException;
import com.cursoonline.exception.evaluacion.EvaluacionNoEncontradaException;
import com.cursoonline.exception.evaluacion.EvaluacionYaExisteException;
import com.cursoonline.repository.academico.RelProfesorSeccionRepository;
import com.cursoonline.repository.academico.TraModuloRepository;
import com.cursoonline.repository.evaluacion.TraEvaluacionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EvaluacionService {

    private final TraEvaluacionRepository       evaluacionRepository;
    private final TraModuloRepository           moduloRepository;
    private final RelProfesorSeccionRepository  profesorSeccionRepository;

    public List<EvaluacionResponse> listarPorModulo(Integer idModulo, SegUsuario usuario) {
        TraModulo modulo = moduloRepository.findById(idModulo)
                .orElseThrow(() -> new ModuloNoEncontradoException(idModulo));
        validarAccesoAlCurso(usuario, modulo.getCurso().getIdCurso());

        return evaluacionRepository
                .findByModulo_IdModuloOrderByDesTitulo(idModulo)
                .stream().map(this::toResponse).toList();
    }

    public EvaluacionResponse obtener(Integer id, SegUsuario usuario) {
        TraEvaluacion eval = evaluacionRepository.findById(id)
                .orElseThrow(() -> new EvaluacionNoEncontradaException(id));
        validarAccesoAlCurso(usuario, eval.getModulo().getCurso().getIdCurso());
        return toResponse(eval);
    }

    @Transactional
    public EvaluacionResponse crear(EvaluacionRequest req, SegUsuario usuario) {
        TraModulo modulo = moduloRepository.findById(req.idModulo())
                .orElseThrow(() -> new ModuloNoEncontradoException(req.idModulo()));
        validarAccesoAlCurso(usuario, modulo.getCurso().getIdCurso());

        if (evaluacionRepository.existsByModulo_IdModuloAndDesTitulo(
                req.idModulo(), req.desTitulo())) {
            throw new EvaluacionYaExisteException(req.desTitulo());
        }

        TraEvaluacion eval = TraEvaluacion.builder()
                .modulo(modulo)
                .desTitulo(req.desTitulo())
                .desInstrucciones(req.desInstrucciones())
                .valPuntajeMinimo(req.valPuntajeMinimo())
                .valTiempoLimite(req.valTiempoLimite())
                .valMaxIntentos(req.valMaxIntentos())
                .estActiva(false)
                .build();

        evaluacionRepository.save(eval);
        log.info("Evaluación creada → {} (módulo: {})",
                eval.getDesTitulo(), modulo.getDesNombre());
        return toResponse(eval);
    }

    @Transactional
    public EvaluacionResponse actualizar(Integer id, EvaluacionRequest req, SegUsuario usuario) {
        TraEvaluacion eval = evaluacionRepository.findById(id)
                .orElseThrow(() -> new EvaluacionNoEncontradaException(id));
        validarAccesoAlCurso(usuario, eval.getModulo().getCurso().getIdCurso());

        if (Boolean.TRUE.equals(eval.getEstActiva())) {
            throw new EvaluacionActivaException();
        }

        eval.setDesTitulo(req.desTitulo());
        eval.setDesInstrucciones(req.desInstrucciones());
        eval.setValPuntajeMinimo(req.valPuntajeMinimo());
        eval.setValTiempoLimite(req.valTiempoLimite());
        eval.setValMaxIntentos(req.valMaxIntentos());

        evaluacionRepository.save(eval);
        log.info("Evaluación actualizada → {}", eval.getDesTitulo());
        return toResponse(eval);
    }

    @Transactional
    public EvaluacionResponse activar(Integer id, SegUsuario usuario) {
        return cambiarEstado(id, usuario, true);
    }

    @Transactional
    public EvaluacionResponse desactivar(Integer id, SegUsuario usuario) {
        return cambiarEstado(id, usuario, false);
    }

    private EvaluacionResponse cambiarEstado(Integer id, SegUsuario usuario, boolean activa) {
        TraEvaluacion eval = evaluacionRepository.findById(id)
                .orElseThrow(() -> new EvaluacionNoEncontradaException(id));
        validarAccesoAlCurso(usuario, eval.getModulo().getCurso().getIdCurso());
        eval.setEstActiva(activa);
        evaluacionRepository.save(eval);
        log.info("Evaluación {} → {}", activa ? "activada" : "desactivada", eval.getDesTitulo());
        return toResponse(eval);
    }

    // ── Validación de acceso (mismo patrón que ModuloService) ───────────────

    private void validarAccesoAlCurso(SegUsuario usuario, Integer idCurso) {
        if ("ROL_ADMIN".equals(usuario.getRol().getCodRol())) return;
        if ("ROL_PROFESOR".equals(usuario.getRol().getCodRol())) {
            boolean tieneAcceso = profesorSeccionRepository
                    .profesorTieneAccesoACurso(usuario.getIdUsuario(), idCurso);
            if (!tieneAcceso) throw new AccesoCursoDenegadoException();
            return;
        }
        throw new AccesoCursoDenegadoException();
    }

    private EvaluacionResponse toResponse(TraEvaluacion e) {
        return new EvaluacionResponse(
                e.getIdEvaluacion(),
                e.getModulo() != null ? e.getModulo().getIdModulo() : null,
                e.getModulo() != null ? e.getModulo().getDesNombre() : null,
                e.getDesTitulo(),
                e.getDesInstrucciones(),
                e.getValPuntajeMinimo(),
                e.getValTiempoLimite(),
                e.getValMaxIntentos(),
                e.getEstActiva(),
                e.getFecCreacion()
        );
    }
}