package com.cursoonline.entity.evaluacion;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tra_opcion_respuesta")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TraOpcionRespuesta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_opcion")
    private Integer idOpcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private TraPregunta pregunta;

    @Column(name = "des_opcion", columnDefinition = "TEXT", nullable = false)
    private String desOpcion;

    @Column(name = "est_correcta", nullable = false)
    private Boolean estCorrecta = false;

    @Column(name = "val_orden", nullable = false)
    private Short valOrden = 1;
}