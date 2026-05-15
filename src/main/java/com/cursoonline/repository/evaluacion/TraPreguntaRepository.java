package com.cursoonline.repository.evaluacion;

import com.cursoonline.entity.evaluacion.TraPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TraPreguntaRepository extends JpaRepository<TraPregunta, Integer> {
    List<TraPregunta> findByEvaluacion_IdEvaluacionAndEstActivaTrueOrderByValOrden(Integer idEvaluacion);
}