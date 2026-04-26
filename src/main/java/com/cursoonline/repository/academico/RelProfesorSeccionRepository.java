package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.RelProfesorSeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface RelProfesorSeccionRepository extends JpaRepository<RelProfesorSeccion, Integer> {

    Optional<RelProfesorSeccion> findBySeccion_IdSeccionAndEstActivoTrue(Integer idSeccion);

    List<RelProfesorSeccion> findByProfesor_IdUsuarioAndEstActivoTrue(Integer idUsuario);

    boolean existsByProfesor_IdUsuarioAndSeccion_IdSeccionAndEstActivoTrue(
            Integer idUsuario, Integer idSeccion);
    
            @Query("""
       SELECT COUNT(rps) > 0
       FROM   RelProfesorSeccion rps
       WHERE  rps.profesor.idUsuario  = :idUsuario
         AND  rps.seccion.curso.idCurso = :idCurso
         AND  rps.estActivo            = true
         AND  rps.seccion.estActiva    = true
       """)
boolean profesorTieneAccesoACurso(
        @Param("idUsuario") Integer idUsuario,
        @Param("idCurso") Integer idCurso);
}