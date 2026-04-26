package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.CatNivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CatNivelRepository extends JpaRepository<CatNivel, Integer> {
    List<CatNivel> findByEstActivoTrueOrderByValOrdenAsc();
}