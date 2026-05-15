package com.cursoonline.service.evaluacion;

import com.cursoonline.dto.evaluacion.request.RespuestaAlumnoRequest;
import com.cursoonline.dto.evaluacion.response.*;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.entity.evaluacion.*;
import com.cursoonline.exception.academico.AccesoCursoDenegadoException;
import com.cursoonline.exception.evaluacion.*;
import com.cursoonline.repository.academico.RelAlumnoSeccionRepository;
import com.cursoonline.repository.evaluacion.*;
import com.cursoonline.repository.progreso.TraProgresoLeccionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cursoonline.dto.evaluacion.response.OpcionRespuestaResponse;
import com.cursoonline.exception.evaluacion.EvaluacionNoEncontradaException;
import com.cursoonline.exception.evaluacion.PreguntaNoEncontradaException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IntentoService {

    private final TraIntentoEvaluacionRepository  intentoRepository;
    private final TraRespuestaAlumnoRepository    respuestaRepository;
    private final TraEvaluacionRepository         evaluacionRepository;
    private final TraPreguntaRepository           preguntaRepository;
    private final TraOpcionRespuestaRepository    opcionRepository;
    private final TraProgresoLeccionRepository    progresoRepository;
    private final RelAlumnoSeccionRepository      alumnoSeccionRepository;

    private static final String TIPO_OPCION_MULTIPLE  = "OPCION_MULTIPLE";
    private static final String TIPO_VERDADERO_FALSO  = "VERDADERO_FALSO";
    private static final String TIPO_COMPLETAR_CODIGO = "COMPLETAR_CODIGO";

    // ── Iniciar o reanudar ──────────────────────────────────────────────────

    @Transactional
    public IntentoEnCursoResponse iniciarOReanudar(Integer idEvaluacion, SegUsuario alumno) {
        TraEvaluacion eval = evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new EvaluacionNoEncontradaException(idEvaluacion));

        if (!Boolean.TRUE.equals(eval.getEstActiva())) {
            throw new EvaluacionNoActivaException();
        }

        Integer idCurso = eval.getModulo().getCurso().getIdCurso();
        if (!alumnoSeccionRepository.alumnoTieneAccesoACurso(alumno.getIdUsuario(), idCurso)) {
            throw new AccesoCursoDenegadoException();
        }

        long pendientes = progresoRepository.countLeccionesObligatoriasPendientes(
                alumno.getIdUsuario(), eval.getModulo().getIdModulo());
        if (pendientes > 0) {
            throw new LeccionesPendientesException(pendientes);
        }

        // ¿Hay intento en curso?
        Optional<TraIntentoEvaluacion> enCurso = intentoRepository
                .findByEvaluacion_IdEvaluacionAndUsuario_IdUsuarioAndEstCompletadoFalse(
                        idEvaluacion, alumno.getIdUsuario());

        if (enCurso.isPresent()) {
            TraIntentoEvaluacion intento = enCurso.get();
            cerrarSiExpirado(intento);
            if (!Boolean.TRUE.equals(intento.getEstCompletado())) {
                return toEnCursoResponse(intento);
            }
        }

        // Crear nuevo
        long completados = intentoRepository
                .countByEvaluacion_IdEvaluacionAndUsuario_IdUsuarioAndEstCompletadoTrue(
                        idEvaluacion, alumno.getIdUsuario());
        if (completados >= eval.getValMaxIntentos()) {
            throw new MaxIntentosAlcanzadosException(eval.getValMaxIntentos());
        }

        TraIntentoEvaluacion nuevo = TraIntentoEvaluacion.builder()
                .evaluacion(eval)
                .usuario(alumno)
                .numIntento((short) (completados + 1))
                .estCompletado(false)
                .fecInicio(LocalDateTime.now())
                .build();
        intentoRepository.save(nuevo);

        log.info("Intento iniciado → eval: {}, alumno: {}, num: {}",
                eval.getDesTitulo(), alumno.getDesCorreo(), nuevo.getNumIntento());

        return toEnCursoResponse(nuevo);
    }

    // ── Obtener intento en curso ────────────────────────────────────────────

    @Transactional
    public IntentoEnCursoResponse obtenerIntentoEnCurso(Integer idIntento, SegUsuario alumno) {
        TraIntentoEvaluacion intento = cargarYValidarIntento(idIntento, alumno);
        cerrarSiExpirado(intento);
        if (Boolean.TRUE.equals(intento.getEstCompletado())) {
            throw new IntentoCompletadoException();
        }
        return toEnCursoResponse(intento);
    }

    // ── Guardar respuesta ───────────────────────────────────────────────────

    @Transactional
    public void guardarRespuesta(Integer idIntento, Integer idPregunta,
                                  RespuestaAlumnoRequest req, SegUsuario alumno) {
        TraIntentoEvaluacion intento = cargarYValidarIntento(idIntento, alumno);
        cerrarSiExpirado(intento);
        if (Boolean.TRUE.equals(intento.getEstCompletado())) {
            throw new IntentoCompletadoException();
        }

        TraPregunta pregunta = preguntaRepository.findById(idPregunta)
                .orElseThrow(() -> new PreguntaNoEncontradaException(idPregunta));

        if (!pregunta.getEvaluacion().getIdEvaluacion().equals(intento.getEvaluacion().getIdEvaluacion())) {
            throw new RespuestaInvalidaException("La pregunta no pertenece a la evaluación del intento.");
        }

        String tipo = pregunta.getTipoPregunta().getCodTipo();
        TraOpcionRespuesta opcion = null;

        if (TIPO_COMPLETAR_CODIGO.equals(tipo)) {
            if (req.idOpcionElegida() != null || req.desRespuestaTexto() == null
                    || req.desRespuestaTexto().isBlank()) {
                throw new RespuestaInvalidaException(
                        "Para completar código debes enviar 'desRespuestaTexto' y dejar 'idOpcionElegida' en null.");
            }
        } else {
            if (req.idOpcionElegida() == null || req.desRespuestaTexto() != null) {
                throw new RespuestaInvalidaException(
                        "Para esta pregunta debes enviar 'idOpcionElegida' y dejar 'desRespuestaTexto' en null.");
            }
            opcion = opcionRepository.findById(req.idOpcionElegida())
                    .orElseThrow(() -> new RespuestaInvalidaException(
                            "La opción " + req.idOpcionElegida() + " no existe."));
            if (!opcion.getPregunta().getIdPregunta().equals(idPregunta)) {
                throw new RespuestaInvalidaException("La opción no pertenece a la pregunta indicada.");
            }
        }

        Optional<TraRespuestaAlumno> existente = respuestaRepository
                .findByIntento_IdIntentoAndPregunta_IdPregunta(idIntento, idPregunta);

        TraRespuestaAlumno r = existente.orElseGet(() ->
                TraRespuestaAlumno.builder()
                        .intento(intento)
                        .pregunta(pregunta)
                        .build());
        r.setOpcionElegida(opcion);
        r.setDesRespuestaTexto(req.desRespuestaTexto());
        r.setFecRespuesta(LocalDateTime.now());
        respuestaRepository.save(r);
    }

    // ── Finalizar ──────────────────────────────────────────────────────────

    @Transactional
    public IntentoFinalizadoResponse finalizar(Integer idIntento, SegUsuario alumno) {
        TraIntentoEvaluacion intento = cargarYValidarIntento(idIntento, alumno);
        if (Boolean.TRUE.equals(intento.getEstCompletado())) {
            throw new IntentoCompletadoException();
        }
        finalizarYCalcular(intento);
        return toFinalizadoResponse(intento);
    }

    // ── Historial ──────────────────────────────────────────────────────────

    public List<IntentoHistorialItemResponse> obtenerHistorial(Integer idEvaluacion, SegUsuario alumno) {
        return intentoRepository
                .findByEvaluacion_IdEvaluacionAndUsuario_IdUsuarioOrderByNumIntentoDesc(
                        idEvaluacion, alumno.getIdUsuario())
                .stream()
                .map(i -> new IntentoHistorialItemResponse(
                        i.getIdIntento(), i.getNumIntento(), i.getValCalificacion(),
                        i.getEstAprobado(), i.getEstCompletado(),
                        i.getFecInicio(), i.getFecFin()))
                .toList();
    }

    // ── Revisión post-rendición ────────────────────────────────────────────

    public IntentoRevisionResponse obtenerRevision(Integer idIntento, SegUsuario alumno) {
        TraIntentoEvaluacion intento = cargarYValidarIntento(idIntento, alumno);
        if (!Boolean.TRUE.equals(intento.getEstCompletado())) {
            throw new RespuestaInvalidaException(
                    "El intento aún no está finalizado. Finalízalo primero para ver la revisión.");
        }
        return toRevisionResponse(intento);
    }

    // ── Helpers ────────────────────────────────────────────────────────────

    private TraIntentoEvaluacion cargarYValidarIntento(Integer idIntento, SegUsuario alumno) {
        TraIntentoEvaluacion intento = intentoRepository.findById(idIntento)
                .orElseThrow(() -> new IntentoNoEncontradoException(idIntento));
        if (!intento.getUsuario().getIdUsuario().equals(alumno.getIdUsuario())) {
            throw new AccesoCursoDenegadoException();
        }
        return intento;
    }

    private void cerrarSiExpirado(TraIntentoEvaluacion intento) {
        if (Boolean.TRUE.equals(intento.getEstCompletado())) return;
        Short tiempo = intento.getEvaluacion().getValTiempoLimite();
        if (tiempo == null) return;
        LocalDateTime limite = intento.getFecInicio().plusMinutes(tiempo);
        if (LocalDateTime.now().isAfter(limite)) {
            log.info("Intento {} expirado, cerrando automáticamente.", intento.getIdIntento());
            finalizarYCalcular(intento);
        }
    }

    private void finalizarYCalcular(TraIntentoEvaluacion intento) {
        List<TraPregunta> preguntas = preguntaRepository
                .findByEvaluacion_IdEvaluacionAndEstActivaTrueOrderByValOrden(
                        intento.getEvaluacion().getIdEvaluacion());

        Map<Integer, TraRespuestaAlumno> respPorPregunta = respuestaRepository
                .findByIntento_IdIntento(intento.getIdIntento()).stream()
                .collect(Collectors.toMap(
                        r -> r.getPregunta().getIdPregunta(), r -> r, (a, b) -> a));

        BigDecimal puntajeTotal = BigDecimal.ZERO;
        BigDecimal puntajeObtenido = BigDecimal.ZERO;

        for (TraPregunta p : preguntas) {
            puntajeTotal = puntajeTotal.add(p.getValPuntaje());

            TraRespuestaAlumno r = respPorPregunta.get(p.getIdPregunta());
            boolean correcta = false;
            if (r != null) {
                List<TraOpcionRespuesta> ops = opcionRepository
                        .findByPregunta_IdPreguntaOrderByValOrden(p.getIdPregunta());
                correcta = esRespuestaCorrecta(r, p, ops);
                r.setEstCorrecta(correcta);
                respuestaRepository.save(r);
            }
            if (correcta) puntajeObtenido = puntajeObtenido.add(p.getValPuntaje());
        }

        BigDecimal calificacion = (puntajeTotal.compareTo(BigDecimal.ZERO) == 0)
                ? BigDecimal.ZERO
                : puntajeObtenido.multiply(BigDecimal.valueOf(100))
                        .divide(puntajeTotal, 2, RoundingMode.HALF_UP);

        boolean aprobado = calificacion.compareTo(intento.getEvaluacion().getValPuntajeMinimo()) >= 0;

        intento.setValCalificacion(calificacion);
        intento.setEstAprobado(aprobado);
        intento.setEstCompletado(true);
        intento.setFecFin(LocalDateTime.now());
        intentoRepository.save(intento);

        log.info("Intento finalizado → ID {}, calificación: {}, aprobado: {}",
                intento.getIdIntento(), calificacion, aprobado);
    }

    private boolean esRespuestaCorrecta(TraRespuestaAlumno r, TraPregunta pregunta,
                                         List<TraOpcionRespuesta> opciones) {
        String tipo = pregunta.getTipoPregunta().getCodTipo();
        if (TIPO_COMPLETAR_CODIGO.equals(tipo)) {
            if (r.getDesRespuestaTexto() == null) return false;
            String esperada = opciones.stream()
                    .filter(o -> Boolean.TRUE.equals(o.getEstCorrecta()))
                    .findFirst().map(TraOpcionRespuesta::getDesOpcion).orElse(null);
            return esperada != null
                    && normalizar(r.getDesRespuestaTexto()).equals(normalizar(esperada));
        }
        if (r.getOpcionElegida() == null) return false;
        Integer idElegida = r.getOpcionElegida().getIdOpcion();
        return opciones.stream()
                .filter(o -> o.getIdOpcion().equals(idElegida))
                .findFirst()
                .map(o -> Boolean.TRUE.equals(o.getEstCorrecta()))
                .orElse(false);
    }

    private String normalizar(String s) {
        return s == null ? "" : s.trim().replaceAll("\\s+", " ");
    }

    private Integer calcularMinutosRestantes(TraIntentoEvaluacion intento) {
        Short tiempo = intento.getEvaluacion().getValTiempoLimite();
        if (tiempo == null) return null;
        LocalDateTime limite = intento.getFecInicio().plusMinutes(tiempo);
        long minutos = java.time.Duration.between(LocalDateTime.now(), limite).toMinutes();
        return (int) Math.max(0, minutos);
    }

    // ── Mappers ────────────────────────────────────────────────────────────

    private IntentoEnCursoResponse toEnCursoResponse(TraIntentoEvaluacion intento) {
        Integer idEval = intento.getEvaluacion().getIdEvaluacion();

        List<PreguntaParaRendirResponse> preguntas = preguntaRepository
                .findByEvaluacion_IdEvaluacionAndEstActivaTrueOrderByValOrden(idEval)
                .stream()
                .map(p -> new PreguntaParaRendirResponse(
                        p.getIdPregunta(),
                        p.getTipoPregunta().getCodTipo(),
                        p.getDesEnunciado(),
                        p.getValOrden(),
                        p.getValPuntaje(),
                        opcionRepository.findByPregunta_IdPreguntaOrderByValOrden(p.getIdPregunta())
                                .stream()
                                .map(o -> new OpcionParaRendirResponse(
                                        o.getIdOpcion(), o.getDesOpcion(), o.getValOrden()))
                                .toList()))
                .toList();

        List<RespuestaActualResponse> misRespuestas = respuestaRepository
                .findByIntento_IdIntento(intento.getIdIntento())
                .stream()
                .map(r -> new RespuestaActualResponse(
                        r.getPregunta().getIdPregunta(),
                        r.getOpcionElegida() != null ? r.getOpcionElegida().getIdOpcion() : null,
                        r.getDesRespuestaTexto()))
                .toList();

        return new IntentoEnCursoResponse(
                intento.getIdIntento(),
                idEval,
                intento.getEvaluacion().getDesTitulo(),
                intento.getEvaluacion().getDesInstrucciones(),
                intento.getEvaluacion().getValPuntajeMinimo(),
                intento.getNumIntento(),
                calcularMinutosRestantes(intento),
                intento.getFecInicio(),
                preguntas,
                misRespuestas
        );
    }

    private IntentoFinalizadoResponse toFinalizadoResponse(TraIntentoEvaluacion intento) {
        return new IntentoFinalizadoResponse(
                intento.getIdIntento(),
                intento.getEvaluacion().getIdEvaluacion(),
                intento.getEvaluacion().getDesTitulo(),
                intento.getValCalificacion(),
                intento.getEvaluacion().getValPuntajeMinimo(),
                intento.getEstAprobado(),
                intento.getNumIntento(),
                intento.getFecInicio(),
                intento.getFecFin()
        );
    }

    private IntentoRevisionResponse toRevisionResponse(TraIntentoEvaluacion intento) {
        Integer idEval = intento.getEvaluacion().getIdEvaluacion();

        Map<Integer, TraRespuestaAlumno> respPorPregunta = respuestaRepository
                .findByIntento_IdIntento(intento.getIdIntento()).stream()
                .collect(Collectors.toMap(
                        r -> r.getPregunta().getIdPregunta(), r -> r, (a, b) -> a));

        List<PreguntaRevisionResponse> preguntas = preguntaRepository
                .findByEvaluacion_IdEvaluacionAndEstActivaTrueOrderByValOrden(idEval)
                .stream()
                .map(p -> {
                    TraRespuestaAlumno r = respPorPregunta.get(p.getIdPregunta());
                    List<OpcionRespuestaResponse> ops = opcionRepository
                            .findByPregunta_IdPreguntaOrderByValOrden(p.getIdPregunta())
                            .stream()
                            .map(o -> new OpcionRespuestaResponse(
                                    o.getIdOpcion(), o.getDesOpcion(),
                                    o.getEstCorrecta(), o.getValOrden()))
                            .toList();
                    return new PreguntaRevisionResponse(
                            p.getIdPregunta(),
                            p.getTipoPregunta().getCodTipo(),
                            p.getDesEnunciado(),
                            p.getValPuntaje(),
                            r != null && r.getOpcionElegida() != null
                                    ? r.getOpcionElegida().getIdOpcion() : null,
                            r != null ? r.getDesRespuestaTexto() : null,
                            r != null ? r.getEstCorrecta() : null,
                            ops);
                })
                .toList();

        return new IntentoRevisionResponse(
                intento.getIdIntento(),
                idEval,
                intento.getEvaluacion().getDesTitulo(),
                intento.getValCalificacion(),
                intento.getEvaluacion().getValPuntajeMinimo(),
                intento.getEstAprobado(),
                intento.getNumIntento(),
                intento.getFecInicio(),
                intento.getFecFin(),
                preguntas
        );
    }
}