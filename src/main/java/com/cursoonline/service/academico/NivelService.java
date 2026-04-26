package com.cursoonline.service.academico;

import com.cursoonline.dto.academico.request.NivelRequest;
import com.cursoonline.dto.academico.response.NivelResponse;
import com.cursoonline.entity.academico.CatNivel;
import com.cursoonline.exception.academico.NivelNoEncontradoException;
import com.cursoonline.repository.academico.CatNivelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import jakarta.transaction.Transactional;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NivelService {

    private final CatNivelRepository nivelRepository;

    public List<NivelResponse> listar() {
        return nivelRepository.findByEstActivoTrueOrderByValOrdenAsc()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NivelResponse obtener(Integer id) {
        return toResponse(
                nivelRepository.findById(id)
                        .orElseThrow(() -> new NivelNoEncontradoException(id))
        );
    }

    @Transactional
    public NivelResponse crear(NivelRequest request) {
        CatNivel nivel = CatNivel.builder()
                .codNivel(request.codNivel().toUpperCase())
                .desNombre(request.desNombre())
                .valOrden(request.valOrden())
                .estActivo(true)
                .build();
        nivelRepository.save(nivel);
        log.info("Nivel creado → {}", nivel.getCodNivel());
        return toResponse(nivel);
    }

    @Transactional
    public NivelResponse actualizar(Integer id, NivelRequest request) {
        CatNivel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new NivelNoEncontradoException(id));
        nivel.setCodNivel(request.codNivel().toUpperCase());
        nivel.setDesNombre(request.desNombre());
        nivel.setValOrden(request.valOrden());
        nivelRepository.save(nivel);
        log.info("Nivel actualizado → {}", nivel.getCodNivel());
        return toResponse(nivel);
    }

    private NivelResponse toResponse(CatNivel n) {
        return new NivelResponse(
                n.getIdNivel(),
                n.getCodNivel(),
                n.getDesNombre(),
                n.getValOrden(),
                n.getEstActivo()
        );
    }
}