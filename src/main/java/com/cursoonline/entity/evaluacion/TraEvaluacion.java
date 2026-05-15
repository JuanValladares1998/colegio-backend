package com.cursoonline.entity.evaluacion;

import com.cursoonline.entity.academico.TraModulo;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tra_evaluacion")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TraEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_evaluacion")
    private Integer idEvaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modulo", nullable = false)
    private TraModulo modulo;

    @Column(name = "des_titulo", length = 150, nullable = false)
    private String desTitulo;

    @Column(name = "des_instrucciones", columnDefinition = "TEXT")
    private String desInstrucciones;

    @Column(name = "val_puntaje_minimo", precision = 5, scale = 2, nullable = false)
    private BigDecimal valPuntajeMinimo;

    @Column(name = "val_tiempo_limite")
    private Short valTiempoLimite;

    @Column(name = "val_max_intentos", nullable = false)
    private Short valMaxIntentos;

    @Column(name = "est_activa", nullable = false)
    private Boolean estActiva = false;

    @Column(name = "fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fecCreacion;

    @PrePersist
    protected void onCreate() { fecCreacion = LocalDateTime.now(); }
}