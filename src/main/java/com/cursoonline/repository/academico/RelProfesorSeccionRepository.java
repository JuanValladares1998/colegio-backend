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

    @Query("""
    SELECT rps FROM RelProfesorSeccion rps
    JOIN FETCH rps.profesor u
    JOIN FETCH rps.seccion s
    JOIN FETCH s.curso
    JOIN FETCH s.anioEscolar
    WHERE rps.estActivo = true
      AND (:idProfesor IS NULL OR u.idUsuario = :idProfesor)
      AND (:idSeccion  IS NULL OR s.idSeccion = :idSeccion)
      AND (:idCurso    IS NULL OR s.curso.idCurso = :idCurso)
      AND (:idAnio     IS NULL OR s.anioEscolar.idAnioEscolar = :idAnio)
    ORDER BY u.desApellidos, s.desNombre
""")
List<RelProfesorSeccion> findAsignacionesConFiltros(
        @Param("idProfesor") Integer idProfesor,
        @Param("idSeccion") Integer idSeccion,
        @Param("idCurso") Integer idCurso,
        @Param("idAnio") Integer idAnio);
}