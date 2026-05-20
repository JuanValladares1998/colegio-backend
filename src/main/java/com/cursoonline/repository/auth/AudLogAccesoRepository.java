package com.cursoonline.repository.auth;

import com.cursoonline.entity.auth.AudLogAcceso;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface AudLogAccesoRepository extends JpaRepository<AudLogAcceso, Integer> {

   @Query("""
        SELECT a FROM AudLogAcceso a
        WHERE (cast(:desde as localdatetime) IS NULL OR a.fecIntento >= :desde)
          AND (cast(:hasta as localdatetime) IS NULL OR a.fecIntento <= :hasta)
        ORDER BY a.fecIntento DESC
    """)
    List<AudLogAcceso> findRangoFechas(
            @Param("desde") LocalDateTime desde,
            @Param("hasta") LocalDateTime hasta);
}
