package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.CatTipoRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CatTipoRecursoRepository extends JpaRepository<CatTipoRecurso, Integer> {

    List<CatTipoRecurso> findByEstActivoTrue();

    Optional<CatTipoRecurso> findByCodTipo(String codTipo);
}