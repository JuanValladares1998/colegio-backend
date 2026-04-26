package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.CatAnioEscolar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CatAnioEscolarRepository extends JpaRepository<CatAnioEscolar, Integer> {
    List<CatAnioEscolar> findByEstActivoTrueOrderByValAnioDesc();
}