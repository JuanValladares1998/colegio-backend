package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "cat_anio_escolar")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatAnioEscolar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_anio_escolar")
    private Integer idAnioEscolar;

    @Column(name = "val_anio", nullable = false, unique = true)
    private Short valAnio;

    @Column(name = "des_descripcion", length = 100)
    private String desDescripcion;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;

    @Column(name = "fec_inicio")
    private LocalDate fecInicio;

    @Column(name = "fec_fin")
    private LocalDate fecFin;
}