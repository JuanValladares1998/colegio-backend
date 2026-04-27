package com.cursoonline.repository.progreso;

import com.cursoonline.entity.progreso.TraProgresoLeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TraProgresoLeccionRepository extends JpaRepository<TraProgresoLeccion, Integer> {

    /**
     * Busca el registro de progreso de un alumno en una lección específica.
     * Usado en {@code marcarLeccionCompletada} para detectar idempotencia.
     */
    Optional<TraProgresoLeccion> findByUsuario_IdUsuarioAndLeccion_IdLeccion(
            Integer idUsuario, Integer idLeccion);

    /**
     * Estadísticas de avance por módulo del alumno en un curso (CUS-14).
     * Solo cuenta lecciones obligatorias, publicadas y activas.
     * Módulos sin lecciones obligatorias NO aparecen en el resultado
     * (el JOIN los filtra). Esto es deseado: un módulo sin lecciones
     * obligatorias no tiene sentido mostrarlo en el avance.
     */
    @Query("""
        SELECT m.idModulo                                                AS idModulo,
               m.desNombre                                               AS nombre,
               m.valOrden                                                AS orden,
               COUNT(DISTINCT l.idLeccion)                               AS total,
               COUNT(DISTINCT CASE WHEN pl.estCompletada = true
                                   THEN l.idLeccion END)                 AS completadas
        FROM   TraModulo m
        JOIN   TraLeccion l                  ON l.modulo  = m
                                            AND l.estObligatoria = true
                                            AND l.estPublicada   = true
                                            AND l.estActiva      = true
        LEFT JOIN TraProgresoLeccion pl      ON pl.leccion = l
                                            AND pl.usuario.idUsuario = :idUsuario
        WHERE  m.curso.idCurso = :idCurso
          AND  m.estActivo = true
        GROUP BY m.idModulo, m.desNombre, m.valOrden
        ORDER BY m.valOrden
    """)
    List<ModuloStatsView> findModuloStatsPorAlumnoYCurso(
            @Param("idUsuario") Integer idUsuario,
            @Param("idCurso") Integer idCurso);

    /**
     * Última actividad del alumno en un curso: MAX(fec_completado) sobre las
     * lecciones del curso. Devuelve null si el alumno nunca completó nada.
     */
    @Query("""
        SELECT MAX(pl.fecCompletado)
        FROM   TraProgresoLeccion pl
        WHERE  pl.usuario.idUsuario = :idUsuario
          AND  pl.leccion.modulo.curso.idCurso = :idCurso
          AND  pl.estCompletada = true
    """)
    LocalDateTime findUltimaActividadEnCurso(
            @Param("idUsuario") Integer idUsuario,
            @Param("idCurso") Integer idCurso);

    /** Proyección Spring Data para la query de stats por módulo. */
    interface ModuloStatsView {
        Integer getIdModulo();
        String  getNombre();
        Short   getOrden();
        Long    getTotal();
        Long    getCompletadas();
    }
}   