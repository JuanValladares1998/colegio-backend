package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.TraRecurso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraRecursoRepository extends JpaRepository<TraRecurso, Integer> {

    List<TraRecurso> findByLeccion_IdLeccionAndEstActivoTrueOrderByIdRecurso(Integer idLeccion);
}