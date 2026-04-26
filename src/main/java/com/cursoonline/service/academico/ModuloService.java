package com.cursoonline.service.academico;

import com.cursoonline.dto.academico.request.ModuloRequest;
import com.cursoonline.dto.academico.response.ModuloResponse;
import com.cursoonline.entity.academico.CatCurso;
import com.cursoonline.entity.academico.TraModulo;
import com.cursoonline.entity.auth.SegUsuario;
import com.cursoonline.exception.academico.AccesoCursoDenegadoException;
import com.cursoonline.exception.academico.CursoNoEncontradoException;
import com.cursoonline.exception.academico.ModuloNoEncontradoException;
import com.cursoonline.exception.academico.ModuloYaExisteException;
import com.cursoonline.repository.academico.CatCursoRepository;
import com.cursoonline.repository.academico.RelProfesorSeccionRepository;
import com.cursoonline.repository.academico.TraModuloRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ModuloService {

    private final TraModuloRepository           moduloRepository;
    private final CatCursoRepository            cursoRepository;
    private final RelProfesorSeccionRepository  profesorSeccionRepository;
     

    public List<ModuloResponse> listarPorCurso(Integer idCurso) {
        return moduloRepository
                .findByCurso_IdCursoAndEstActivoTrueOrderByValOrden(idCurso)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ModuloResponse obtener(Integer id) {
        return toResponse(
                moduloRepository.findById(id)
                        .orElseThrow(() -> new ModuloNoEncontradoException(id))
        );
    }

    @Transactional
    public ModuloResponse crear(ModuloRequest request, SegUsuario usuario) {
        validarAccesoAlCurso(usuario, request.idCurso());

        if (moduloRepository.existsByCurso_IdCursoAndDesNombreAndEstActivoTrue(
                request.idCurso(), request.desNombre())) {
            throw new ModuloYaExisteException(request.desNombre());
        }

        CatCurso curso = cursoRepository.findById(request.idCurso())
                .orElseThrow(() -> new CursoNoEncontradoException(request.idCurso()));

        TraModulo modulo = TraModulo.builder()
                .curso(curso)
                .desNombre(request.desNombre())
                .desDescripcion(request.desDescripcion())
                .valOrden(request.valOrden())
                .estActivo(true)
                .build();

        moduloRepository.save(modulo);
        log.info("Módulo creado → {} (curso: {})", modulo.getDesNombre(), curso.getDesNombre());
        return toResponse(modulo);
    }

    @Transactional
    public ModuloResponse actualizar(Integer id, ModuloRequest request, SegUsuario usuario) {
        TraModulo modulo = moduloRepository.findById(id)
                .orElseThrow(() -> new ModuloNoEncontradoException(id));

        validarAccesoAlCurso(usuario, modulo.getCurso().getIdCurso());

        modulo.setDesNombre(request.desNombre());
        modulo.setDesDescripcion(request.desDescripcion());
        modulo.setValOrden(request.valOrden());

        moduloRepository.save(modulo);
        log.info("Módulo actualizado → {}", modulo.getDesNombre());
        return toResponse(modulo);
    }

    @Transactional
    public void eliminar(Integer id, SegUsuario usuario) {
        TraModulo modulo = moduloRepository.findById(id)
                .orElseThrow(() -> new ModuloNoEncontradoException(id));

        validarAccesoAlCurso(usuario, modulo.getCurso().getIdCurso());

        modulo.setEstActivo(false);
        moduloRepository.save(modulo);
        log.info("Módulo eliminado lógicamente → {}", modulo.getDesNombre());
    }

    // ── Validación de acceso ──────────────────────────────────────────────────

    private void validarAccesoAlCurso(SegUsuario usuario, Integer idCurso) {
        // Admin pasa libre
        if ("ROL_ADMIN".equals(usuario.getRol().getCodRol())) return;

        // Profesor: debe tener al menos una sección activa de este curso
        if ("ROL_PROFESOR".equals(usuario.getRol().getCodRol())) {
            boolean tieneAcceso = profesorSeccionRepository
                    .profesorTieneAccesoACurso(usuario.getIdUsuario(), idCurso);
            if (!tieneAcceso) throw new AccesoCursoDenegadoException();
            return;
        }

        // Cualquier otro rol: bloqueado
        throw new AccesoCursoDenegadoException();
    }

    private ModuloResponse toResponse(TraModulo m) {
        return new ModuloResponse(
                m.getIdModulo(),
                m.getCurso() != null ? m.getCurso().getIdCurso() : null,
                m.getCurso() != null ? m.getCurso().getDesNombre() : null,
                m.getDesNombre(),
                m.getDesDescripcion(),
                m.getValOrden(),
                m.getEstActivo(),
                m.getFecCreacion()
        );
    }
}