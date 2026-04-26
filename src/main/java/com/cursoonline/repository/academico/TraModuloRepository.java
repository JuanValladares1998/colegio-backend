package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.TraModulo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraModuloRepository extends JpaRepository<TraModulo, Integer> {

    List<TraModulo> findByCurso_IdCursoAndEstActivoTrueOrderByValOrden(Integer idCurso);

    boolean existsByCurso_IdCursoAndDesNombreAndEstActivoTrue(Integer idCurso, String desNombre);
}