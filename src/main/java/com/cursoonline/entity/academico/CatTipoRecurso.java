package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "cat_tipo_recurso")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatTipoRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_recurso")
    private Integer idTipoRecurso;

    @Column(name = "cod_tipo", length = 20, nullable = false, unique = true)
    private String codTipo;       // ARCHIVO | ENLACE | VIDEO

    @Column(name = "des_nombre", length = 50, nullable = false)
    private String desNombre;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;
}