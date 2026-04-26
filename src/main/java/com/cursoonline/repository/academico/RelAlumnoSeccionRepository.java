package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.CatCurso;
import com.cursoonline.entity.academico.RelAlumnoSeccion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.*;
@Repository
public interface RelAlumnoSeccionRepository extends JpaRepository<RelAlumnoSeccion, Integer> {

    @Query("""
           SELECT DISTINCT c
           FROM   RelAlumnoSeccion ras
                  JOIN ras.seccion s
                  JOIN s.curso c
           WHERE  ras.alumno.idUsuario = :idUsuario
             AND  ras.estActivo  = true
             AND  s.estActiva    = true
             AND  c.estActivo    = true
             AND  c.estPublicado = true
           """)
    Page<CatCurso> findCursosPublicadosByAlumno(
            @Param("idUsuario") Integer idUsuario,
            Pageable pageable);

    @Query("""
       SELECT COUNT(ras) > 0
       FROM   RelAlumnoSeccion ras
       WHERE  ras.alumno.idUsuario       = :idUsuario
         AND  ras.seccion.curso.idCurso  = :idCurso
         AND  ras.estActivo              = true
         AND  ras.seccion.estActiva      = true
       """)
boolean alumnoTieneAccesoACurso(
        @Param("idUsuario") Integer idUsuario,
        @Param("idCurso") Integer idCurso);        

        Optional<RelAlumnoSeccion> findByAlumno_IdUsuarioAndSeccion_IdSeccionAndEstActivoTrue(
        Integer idUsuario, Integer idSeccion);
        List<RelAlumnoSeccion> findBySeccion_IdSeccionAndEstActivoTrueOrderByAlumno_DesApellidos(
        Integer idSeccion);

        @Query("""
       SELECT COUNT(ras) > 0
       FROM   RelAlumnoSeccion ras
       WHERE  ras.alumno.idUsuario                  = :idUsuario
         AND  ras.seccion.anioEscolar.idAnioEscolar = :idAnioEscolar
         AND  ras.estActivo                         = true
         AND  ras.seccion.estActiva                 = true
       """)
boolean alumnoTieneInscripcionEnAnio(
        @Param("idUsuario") Integer idUsuario,
        @Param("idAnioEscolar") Integer idAnioEscolar);
}