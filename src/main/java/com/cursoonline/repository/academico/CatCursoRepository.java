package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.CatCurso;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CatCursoRepository extends JpaRepository<CatCurso, Integer> {
    Page<CatCurso> findByEstActivoTrue(Pageable pageable);
    Page<CatCurso> findByEstActivoTrueAndEstPublicadoTrue(Pageable pageable);
    boolean existsByDesNombre(String desNombre);
}