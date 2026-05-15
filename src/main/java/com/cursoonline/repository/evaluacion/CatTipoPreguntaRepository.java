package com.cursoonline.repository.evaluacion;

import com.cursoonline.entity.evaluacion.CatTipoPregunta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CatTipoPreguntaRepository extends JpaRepository<CatTipoPregunta, Integer> {
    List<CatTipoPregunta> findByEstActivoTrueOrderByDesNombre();
}