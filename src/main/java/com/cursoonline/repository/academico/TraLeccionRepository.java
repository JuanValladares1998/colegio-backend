package com.cursoonline.repository.academico;

import com.cursoonline.entity.academico.TraLeccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TraLeccionRepository extends JpaRepository<TraLeccion, Integer> {

    // Vista admin/profesor: todas las lecciones activas (publicadas o no)
    List<TraLeccion> findByModulo_IdModuloAndEstActivaTrueOrderByValOrden(Integer idModulo);

    // Vista alumno: solo lecciones publicadas
    List<TraLeccion> findByModulo_IdModuloAndEstActivaTrueAndEstPublicadaTrueOrderByValOrden(
            Integer idModulo);

    boolean existsByModulo_IdModuloAndDesNombreAndEstActivaTrue(
            Integer idModulo, String desNombre);
}