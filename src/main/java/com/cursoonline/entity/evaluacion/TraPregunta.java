package com.cursoonline.entity.evaluacion;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tra_pregunta")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TraPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pregunta")
    private Integer idPregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private TraEvaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_pregunta", nullable = false)
    private CatTipoPregunta tipoPregunta;

    @Column(name = "des_enunciado", columnDefinition = "TEXT", nullable = false)
    private String desEnunciado;

    @Column(name = "val_orden", nullable = false)
    private Short valOrden = 1;

    @Column(name = "val_puntaje", precision = 5, scale = 2, nullable = false)
    private BigDecimal valPuntaje;

    @Column(name = "est_activa", nullable = false)
    private Boolean estActiva = true;
}