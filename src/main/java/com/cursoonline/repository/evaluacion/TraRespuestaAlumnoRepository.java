package com.cursoonline.repository.evaluacion;

import com.cursoonline.entity.evaluacion.TraRespuestaAlumno;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TraRespuestaAlumnoRepository extends JpaRepository<TraRespuestaAlumno, Integer> {

    List<TraRespuestaAlumno> findByIntento_IdIntento(Integer idIntento);

    Optional<TraRespuestaAlumno> findByIntento_IdIntentoAndPregunta_IdPregunta(
            Integer idIntento, Integer idPregunta);
}