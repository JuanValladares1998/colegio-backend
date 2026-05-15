package com.cursoonline.repository.evaluacion;

import com.cursoonline.entity.evaluacion.TraIntentoEvaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraIntentoEvaluacionRepository extends JpaRepository<TraIntentoEvaluacion, Integer> {

    Optional<TraIntentoEvaluacion> findByEvaluacion_IdEvaluacionAndUsuario_IdUsuarioAndEstCompletadoFalse(
            Integer idEvaluacion, Integer idUsuario);

    long countByEvaluacion_IdEvaluacionAndUsuario_IdUsuarioAndEstCompletadoTrue(
            Integer idEvaluacion, Integer idUsuario);

    List<TraIntentoEvaluacion> findByEvaluacion_IdEvaluacionAndUsuario_IdUsuarioOrderByNumIntentoDesc(
            Integer idEvaluacion, Integer idUsuario);
}