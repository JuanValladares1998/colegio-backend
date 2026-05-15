package com.cursoonline.service.evaluacion;

import com.cursoonline.dto.evaluacion.request.OpcionRespuestaRequest;
import com.cursoonline.dto.evaluacion.request.PreguntaRequest;
import com.cursoonline.dto.evaluacion.response.OpcionRespuestaResponse;
import com.cursoonline.dto.evaluacion.response.PreguntaResponse;
import com.cursoonline.dto.evaluacion.response.TipoPreguntaResponse;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.entity.evaluacion.CatTipoPregunta;
import com.cursoonline.entity.evaluacion.TraEvaluacion;
import com.cursoonline.entity.evaluacion.TraOpcionRespuesta;
import com.cursoonline.entity.evaluacion.TraPregunta;
import com.cursoonline.exception.academico.AccesoCursoDenegadoException;
import com.cursoonline.exception.evaluacion.*;
import com.cursoonline.repository.academico.RelProfesorSeccionRepository;
import com.cursoonline.repository.evaluacion.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreguntaService {

    private static final String TIPO_OPCION_MULTIPLE  = "OPCION_MULTIPLE";
    private static final String TIPO_VERDADERO_FALSO  = "VERDADERO_FALSO";
    private static final String TIPO_COMPLETAR_CODIGO = "COMPLETAR_CODIGO";

    private final TraPreguntaRepository          preguntaRepository;
    private final TraOpcionRespuestaRepository   opcionRepository;
    private final TraEvaluacionRepository        evaluacionRepository;
    private final CatTipoPreguntaRepository      tipoPreguntaRepository;
    private final RelProfesorSeccionRepository   profesorSeccionRepository;

    // ── Lecturas ─────────────────────────────────────────────────────────────

    public List<PreguntaResponse> listarPorEvaluacion(Integer idEvaluacion, SegUsuario usuario) {
        TraEvaluacion eval = evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new EvaluacionNoEncontradaException(idEvaluacion));
        validarAccesoAlCurso(usuario, eval.getModulo().getCurso().getIdCurso());

        return preguntaRepository
                .findByEvaluacion_IdEvaluacionAndEstActivaTrueOrderByValOrden(idEvaluacion)
                .stream().map(this::toResponse).toList();
    }

    public PreguntaResponse obtener(Integer id, SegUsuario usuario) {
        TraPregunta pregunta = preguntaRepository.findById(id)
                .orElseThrow(() -> new PreguntaNoEncontradaException(id));
        validarAccesoAlCurso(usuario, pregunta.getEvaluacion().getModulo().getCurso().getIdCurso());
        return toResponse(pregunta);
    }

    public List<TipoPreguntaResponse> listarTipos() {
        return tipoPreguntaRepository.findByEstActivoTrueOrderByDesNombre()
                .stream()
                .map(t -> new TipoPreguntaResponse(t.getIdTipoPregunta(), t.getCodTipo(), t.getDesNombre()))
                .toList();
    }

    // ── Escrituras ───────────────────────────────────────────────────────────

    @Transactional
    public PreguntaResponse crear(PreguntaRequest req, SegUsuario usuario) {
        TraEvaluacion eval = evaluacionRepository.findById(req.idEvaluacion())
                .orElseThrow(() -> new EvaluacionNoEncontradaException(req.idEvaluacion()));
        validarAccesoAlCurso(usuario, eval.getModulo().getCurso().getIdCurso());

        if (Boolean.TRUE.equals(eval.getEstActiva())) {
            throw new EvaluacionActivaException();
        }

        CatTipoPregunta tipo = tipoPreguntaRepository.findById(req.idTipoPregunta())
                .orElseThrow(() -> new TipoPreguntaNoEncontradoException(req.idTipoPregunta()));

        validarOpcionesSegunTipo(tipo.getCodTipo(), req.opciones());

        TraPregunta pregunta = TraPregunta.builder()
                .evaluacion(eval)
                .tipoPregunta(tipo)
                .desEnunciado(req.desEnunciado())
                .valOrden(req.valOrden())
                .valPuntaje(req.valPuntaje())
                .estActiva(true)
                .build();
        preguntaRepository.save(pregunta);

        crearOpciones(pregunta, req.opciones());

        log.info("Pregunta creada → ID {} (eval: {}, tipo: {})",
                pregunta.getIdPregunta(), eval.getDesTitulo(), tipo.getCodTipo());
        return toResponse(pregunta);
    }

    @Transactional
    public PreguntaResponse actualizar(Integer id, PreguntaRequest req, SegUsuario usuario) {
        TraPregunta pregunta = preguntaRepository.findById(id)
                .orElseThrow(() -> new PreguntaNoEncontradaException(id));
        TraEvaluacion eval = pregunta.getEvaluacion();
        validarAccesoAlCurso(usuario, eval.getModulo().getCurso().getIdCurso());

        if (Boolean.TRUE.equals(eval.getEstActiva())) {
            throw new EvaluacionActivaException();
        }

        CatTipoPregunta tipo = tipoPreguntaRepository.findById(req.idTipoPregunta())
                .orElseThrow(() -> new TipoPreguntaNoEncontradoException(req.idTipoPregunta()));

        validarOpcionesSegunTipo(tipo.getCodTipo(), req.opciones());

        // Reemplazo total de opciones
        opcionRepository.deleteByPreguntaId(pregunta.getIdPregunta());

        pregunta.setTipoPregunta(tipo);
        pregunta.setDesEnunciado(req.desEnunciado());
        pregunta.setValOrden(req.valOrden());
        pregunta.setValPuntaje(req.valPuntaje());
        preguntaRepository.save(pregunta);

        crearOpciones(pregunta, req.opciones());

        log.info("Pregunta actualizada → ID {}", pregunta.getIdPregunta());
        return toResponse(pregunta);
    }

    @Transactional
    public void eliminar(Integer id, SegUsuario usuario) {
        TraPregunta pregunta = preguntaRepository.findById(id)
                .orElseThrow(() -> new PreguntaNoEncontradaException(id));
        TraEvaluacion eval = pregunta.getEvaluacion();
        validarAccesoAlCurso(usuario, eval.getModulo().getCurso().getIdCurso());

        if (Boolean.TRUE.equals(eval.getEstActiva())) {
            throw new EvaluacionActivaException();
        }

        pregunta.setEstActiva(false);
        preguntaRepository.save(pregunta);
        log.info("Pregunta eliminada lógicamente → ID {}", pregunta.getIdPregunta());
    }

    // ── Validaciones de tipo ────────────────────────────────────────────────

    private void validarOpcionesSegunTipo(String codTipo, List<OpcionRespuestaRequest> opciones) {
        long correctas = opciones.stream()
                .filter(o -> Boolean.TRUE.equals(o.estCorrecta())).count();

        switch (codTipo) {
            case TIPO_OPCION_MULTIPLE -> {
                if (opciones.size() < 2)
                    throw new PreguntaInvalidaException(
                            "Una pregunta de opción múltiple debe tener al menos 2 opciones.");
                if (correctas != 1)
                    throw new PreguntaInvalidaException(
                            "Una pregunta de opción múltiple debe tener exactamente 1 opción correcta.");
            }
            case TIPO_VERDADERO_FALSO -> {
                if (opciones.size() != 2)
                    throw new PreguntaInvalidaException(
                            "Una pregunta verdadero/falso debe tener exactamente 2 opciones.");
                if (correctas != 1)
                    throw new PreguntaInvalidaException(
                            "Una pregunta verdadero/falso debe tener exactamente 1 opción correcta.");
            }
            case TIPO_COMPLETAR_CODIGO -> {
                if (opciones.size() != 1 || correctas != 1)
                    throw new PreguntaInvalidaException(
                            "Una pregunta de completar código debe tener exactamente 1 opción correcta con la respuesta esperada.");
            }
            default -> throw new PreguntaInvalidaException("Tipo de pregunta no soportado: " + codTipo);
        }
    }

    private void crearOpciones(TraPregunta pregunta, List<OpcionRespuestaRequest> opciones) {
        for (OpcionRespuestaRequest o : opciones) {
            opcionRepository.save(TraOpcionRespuesta.builder()
                    .pregunta(pregunta)
                    .desOpcion(o.desOpcion())
                    .estCorrecta(o.estCorrecta())
                    .valOrden(o.valOrden())
                    .build());
        }
    }

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

    private PreguntaResponse toResponse(TraPregunta p) {
        List<OpcionRespuestaResponse> ops = opcionRepository
                .findByPregunta_IdPreguntaOrderByValOrden(p.getIdPregunta())
                .stream()
                .map(o -> new OpcionRespuestaResponse(
                        o.getIdOpcion(), o.getDesOpcion(), o.getEstCorrecta(), o.getValOrden()))
                .toList();

        return new PreguntaResponse(
                p.getIdPregunta(),
                p.getEvaluacion().getIdEvaluacion(),
                p.getTipoPregunta().getIdTipoPregunta(),
                p.getTipoPregunta().getCodTipo(),
                p.getDesEnunciado(),
                p.getValOrden(),
                p.getValPuntaje(),
                p.getEstActiva(),
                ops
        );
    }
}