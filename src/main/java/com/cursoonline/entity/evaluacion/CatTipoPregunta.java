package com.cursoonline.entity.evaluacion;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_tipo_pregunta")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CatTipoPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_pregunta")
    private Integer idTipoPregunta;

    @Column(name = "cod_tipo", length = 30, nullable = false, unique = true)
    private String codTipo;

    @Column(name = "des_nombre", length = 60, nullable = false)
    private String desNombre;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}