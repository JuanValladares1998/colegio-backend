package com.cursoonline.service.academico;

import com.cursoonline.dto.academico.response.TipoRecursoResponse;
import com.cursoonline.entity.academico.CatTipoRecurso;
import com.cursoonline.repository.academico.CatTipoRecursoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TipoRecursoService {

    private final CatTipoRecursoRepository tipoRecursoRepository;

    public List<TipoRecursoResponse> listar() {
        return tipoRecursoRepository.findByEstActivoTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TipoRecursoResponse toResponse(CatTipoRecurso t) {
        return new TipoRecursoResponse(
                t.getIdTipoRecurso(),
                t.getCodTipo(),
                t.getDesNombre(),
                t.getEstActivo()
        );
    }
}