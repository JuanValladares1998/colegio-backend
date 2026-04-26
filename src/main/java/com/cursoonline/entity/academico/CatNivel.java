package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_nivel")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatNivel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_nivel")
    private Integer idNivel;

    @Column(name = "cod_nivel", length = 20, nullable = false, unique = true)
    private String codNivel;

    @Column(name = "des_nombre", length = 80, nullable = false)
    private String desNombre;

    @Column(name = "val_orden", nullable = false)
    private Short valOrden = 1;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}