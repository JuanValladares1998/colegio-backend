package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.TraSeccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TraSeccionRepository extends JpaRepository<TraSeccion, Integer> {

    Page<TraSeccion> findByEstActivaTrue(Pageable pageable);

    Page<TraSeccion> findByCurso_IdCursoAndEstActivaTrue(Integer idCurso, Pageable pageable);

    Page<TraSeccion> findByAnioEscolar_IdAnioEscolarAndEstActivaTrue(
            Integer idAnioEscolar, Pageable pageable);

    boolean existsByDesNombreAndCurso_IdCursoAndAnioEscolar_IdAnioEscolar(
            String desNombre, Integer idCurso, Integer idAnioEscolar);
}