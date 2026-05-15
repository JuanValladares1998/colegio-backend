package com.cursoonline.repository.progreso;

import com.cursoonline.entity.progreso.TraProgresoLeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query("""
    SELECT u.idUsuario                                           AS idAlumno,
           u.desNombres                                          AS nombres,
           u.desApellidos                                        AS apellidos,
           (SELECT COUNT(l)
              FROM TraLeccion l
             WHERE l.modulo.curso = s.curso
               AND l.estObligatoria = true
               AND l.estPublicada   = true
               AND l.estActiva      = true)                      AS totalObligatorias,
           (SELECT COUNT(pl)
              FROM TraProgresoLeccion pl
             WHERE pl.usuario = u
               AND pl.estCompletada = true
               AND pl.leccion.modulo.curso = s.curso
               AND pl.leccion.estObligatoria = true
               AND pl.leccion.estPublicada   = true
               AND pl.leccion.estActiva      = true)             AS completadas,
           (SELECT MAX(pl.fecCompletado)
              FROM TraProgresoLeccion pl
             WHERE pl.usuario = u
               AND pl.estCompletada = true
               AND pl.leccion.modulo.curso = s.curso)            AS ultimaActividad
    FROM   RelAlumnoSeccion ras
    JOIN   ras.alumno u
    JOIN   ras.seccion s
    WHERE  s.idSeccion = :idSeccion
      AND  ras.estActivo = true
      AND  u.estActivo   = true
      AND  u.rol.codRol  = 'ROL_ALUMNO'
""")
Page<FilaTableroView> findTableroPorSeccion(
        @Param("idSeccion") Integer idSeccion,
        Pageable pageable);

interface FilaTableroView {
    Integer       getIdAlumno();
    String        getNombres();
    String        getApellidos();
    Long          getTotalObligatorias();
    Long          getCompletadas();
    LocalDateTime getUltimaActividad();
    
}
@Query("""
    SELECT m.idModulo                          AS idModulo,
           m.desNombre                         AS nombreModulo,
           m.valOrden                          AS ordenModulo,
           l.idLeccion                         AS idLeccion,
           l.desNombre                         AS nombreLeccion,
           l.valOrden                          AS ordenLeccion,
           l.estObligatoria                    AS obligatoria,
           COALESCE(pl.estCompletada, false)   AS completada,
           pl.fecCompletado                    AS fecCompletado
    FROM   TraModulo m
    JOIN   TraLeccion l                  ON l.modulo = m
                                        AND l.estPublicada = true
                                        AND l.estActiva    = true
    LEFT JOIN TraProgresoLeccion pl      ON pl.leccion = l
                                        AND pl.usuario.idUsuario = :idAlumno
    WHERE  m.curso.idCurso = :idCurso
      AND  m.estActivo = true
    ORDER BY m.valOrden, l.valOrden
""")
List<HistorialLeccionView> findHistorialPorAlumnoYCurso(
        @Param("idAlumno") Integer idAlumno,
        @Param("idCurso") Integer idCurso);

interface HistorialLeccionView {
    Integer       getIdModulo();
    String        getNombreModulo();
    Short         getOrdenModulo();
    Integer       getIdLeccion();
    String        getNombreLeccion();
    Short         getOrdenLeccion();
    Boolean       getObligatoria();
    Boolean       getCompletada();
    LocalDateTime getFecCompletado();
}
@Query("""
    SELECT COUNT(l)
    FROM   TraLeccion l
    WHERE  l.modulo.idModulo = :idModulo
      AND  l.estObligatoria = true
      AND  l.estPublicada   = true
      AND  l.estActiva      = true
      AND  NOT EXISTS (
            SELECT 1 FROM TraProgresoLeccion pl
            WHERE pl.leccion = l
              AND pl.usuario.idUsuario = :idUsuario
              AND pl.estCompletada = true
      )
""")
long countLeccionesObligatoriasPendientes(
        @Param("idUsuario") Integer idUsuario,
        @Param("idModulo") Integer idModulo);
}   