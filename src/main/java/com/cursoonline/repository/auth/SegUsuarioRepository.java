package com.cursoonline.repository.auth;

import com.cursoonline.entity.auth.SegUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface SegUsuarioRepository extends JpaRepository<SegUsuario, Integer> {

    Optional<SegUsuario> findByDesCorreo(String desCorreo);

    boolean existsByDesCorreo(String desCorreo);

    @Modifying
    @Query("UPDATE SegUsuario u SET u.fecUltimoAcceso = :fecha WHERE u.idUsuario = :id")
    void actualizarUltimoAcceso(@Param("id") Integer id, @Param("fecha") LocalDateTime fecha);

    @Query("""
    SELECT u FROM SegUsuario u
    WHERE (:codRol IS NULL OR u.rol.codRol = :codRol)
      AND (:busqueda IS NULL
           OR LOWER(u.desNombres)   LIKE LOWER(CONCAT('%', :busqueda, '%'))
           OR LOWER(u.desApellidos) LIKE LOWER(CONCAT('%', :busqueda, '%'))
           OR LOWER(u.desCorreo)    LIKE LOWER(CONCAT('%', :busqueda, '%')))
      AND (:estActivo IS NULL OR u.estActivo = :estActivo)
      AND (
          :sinSeccion = false
          OR (u.rol.codRol = 'ROL_ALUMNO'
              AND NOT EXISTS (
                  SELECT 1 FROM RelAlumnoSeccion ras
                  WHERE ras.alumno = u AND ras.estActivo = true
              ))
      )
    """)
    Page<SegUsuario> buscarConFiltros(
            @Param("codRol") String codRol,
            @Param("busqueda") String busqueda,
            @Param("estActivo") Boolean estActivo,
            @Param("sinSeccion") boolean sinSeccion,
            Pageable pageable);

    // Para validar unicidad de correo al actualizar (excluye al propio usuario)
    boolean existsByDesCorreoAndIdUsuarioNot(String correo, Integer idUsuario);
}