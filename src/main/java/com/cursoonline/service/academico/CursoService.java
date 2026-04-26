package com.cursoonline.service.academico;

import com.cursoonline.dto.academico.request.CursoRequest;
import com.cursoonline.dto.academico.response.CursoResponse;
import com.cursoonline.entity.academico.CatCurso;
import com.cursoonline.entity.academico.CatNivel;
import com.cursoonline.exception.academico.CursoNoEncontradoException;
import com.cursoonline.exception.academico.CursoYaExisteException;
import com.cursoonline.exception.academico.NivelNoEncontradoException;
import com.cursoonline.repository.academico.CatCursoRepository;
import com.cursoonline.repository.academico.CatNivelRepository;
import com.cursoonline.repository.academico.RelAlumnoSeccionRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class CursoService {

    private final CatCursoRepository cursoRepository;
    private final CatNivelRepository nivelRepository;
    private final RelAlumnoSeccionRepository alumnoSeccionRepository;

    public Page<CursoResponse> listar(Pageable pageable) {
        return cursoRepository.findByEstActivoTrue(pageable).map(this::toResponse);
    }

    public Page<CursoResponse> listarPublicados(Pageable pageable) {
        return cursoRepository.findByEstActivoTrueAndEstPublicadoTrue(pageable)
                .map(this::toResponse);
    }

    public CursoResponse obtener(Integer id) {
        return toResponse(
                cursoRepository.findById(id)
                        .orElseThrow(() -> new CursoNoEncontradoException(id))
        );
    }

    @Transactional
    public CursoResponse crear(CursoRequest request) {
        if (cursoRepository.existsByDesNombre(request.desNombre()))
            throw new CursoYaExisteException(request.desNombre());

        CatNivel nivel = nivelRepository.findById(request.idNivel())
                .orElseThrow(() -> new NivelNoEncontradoException(request.idNivel()));

        CatCurso curso = CatCurso.builder()
                .nivel(nivel)
                .desNombre(request.desNombre())
                .desDescripcion(request.desDescripcion())
                .estPublicado(false)
                .estActivo(true)
                .build();

        cursoRepository.save(curso);
        log.info("Curso creado → {}", curso.getDesNombre());
        return toResponse(curso);
    }

    @Transactional
    public CursoResponse actualizar(Integer id, CursoRequest request) {
        CatCurso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNoEncontradoException(id));

        CatNivel nivel = nivelRepository.findById(request.idNivel())
                .orElseThrow(() -> new NivelNoEncontradoException(request.idNivel()));

        curso.setNivel(nivel);
        curso.setDesNombre(request.desNombre());
        curso.setDesDescripcion(request.desDescripcion());
        cursoRepository.save(curso);
        log.info("Curso actualizado → {}", curso.getDesNombre());
        return toResponse(curso);
    }

    @Transactional
    public CursoResponse publicar(Integer id) {
        CatCurso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNoEncontradoException(id));

        curso.setEstPublicado(true);
        curso.setFecPublicacion(LocalDateTime.now());
        cursoRepository.save(curso);
        log.info("Curso publicado → {}", curso.getDesNombre());
        return toResponse(curso);
    }

    @Transactional
    public CursoResponse despublicar(Integer id) {
        CatCurso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new CursoNoEncontradoException(id));

        curso.setEstPublicado(false);
        curso.setFecPublicacion(null);
        cursoRepository.save(curso);
        log.info("Curso despublicado → {}", curso.getDesNombre());
        return toResponse(curso);
    }

    private CursoResponse toResponse(CatCurso c) {
        return new CursoResponse(
                c.getIdCurso(),
                c.getNivel() != null ? c.getNivel().getIdNivel() : null,
                c.getNivel() != null ? c.getNivel().getDesNombre() : null,
                c.getDesNombre(),
                c.getDesDescripcion(),
                c.getEstPublicado(),
                c.getEstActivo(),
                c.getFecCreacion(),
                c.getFecPublicacion()
        );
    }
    public Page<CursoResponse> listarPublicadosPorAlumno(Integer idUsuario, Pageable pageable) {
    // Re-mapeamos el Sort para que apunte al alias 'c' (CatCurso) en vez del root 'ras' (RelAlumnoSeccion)
    Sort sortRemapeado = Sort.by(
            pageable.getSort().stream()
                    .map(order -> new Sort.Order(order.getDirection(), "c." + order.getProperty()))
                    .toList()
    );

    Pageable pageableCorregido = PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sortRemapeado.isEmpty() ? Sort.by("c.desNombre").ascending() : sortRemapeado
    );

    return alumnoSeccionRepository
            .findCursosPublicadosByAlumno(idUsuario, pageableCorregido)
            .map(this::toResponse);
}
}