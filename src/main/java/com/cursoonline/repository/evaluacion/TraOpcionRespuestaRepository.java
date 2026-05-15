package com.cursoonline.repository.evaluacion;

import com.cursoonline.entity.evaluacion.TraOpcionRespuesta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface TraOpcionRespuestaRepository extends JpaRepository<TraOpcionRespuesta, Integer> {
    List<TraOpcionRespuesta> findByPregunta_IdPreguntaOrderByValOrden(Integer idPregunta);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("DELETE FROM TraOpcionRespuesta o WHERE o.pregunta.idPregunta = :idPregunta")
    void deleteByPreguntaId(@Param("idPregunta") Integer idPregunta);
}