package com.cursoonline.repository.evaluacion;

import com.cursoonline.entity.evaluacion.TraEvaluacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TraEvaluacionRepository extends JpaRepository<TraEvaluacion, Integer> {
    List<TraEvaluacion> findByModulo_IdModuloOrderByDesTitulo(Integer idModulo);
    boolean existsByModulo_IdModuloAndDesTitulo(Integer idModulo, String desTitulo);
}