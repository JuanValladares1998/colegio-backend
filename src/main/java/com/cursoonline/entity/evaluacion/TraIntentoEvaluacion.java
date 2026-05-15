package com.cursoonline.entity.evaluacion;

import com.cursoonline.entity.auth.SegUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "tra_intento_evaluacion")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TraIntentoEvaluacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_intento")
    private Integer idIntento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_evaluacion", nullable = false)
    private TraEvaluacion evaluacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private SegUsuario usuario;

    @Column(name = "val_calificacion", precision = 5, scale = 2)
    private BigDecimal valCalificacion;

    @Column(name = "num_intento", nullable = false)
    private Short numIntento = 1;

    @Column(name = "est_aprobado")
    private Boolean estAprobado;

    @Column(name = "est_completado", nullable = false)
    private Boolean estCompletado = false;

    @Column(name = "fec_inicio", nullable = false)
    private LocalDateTime fecInicio;

    @Column(name = "fec_fin")
    private LocalDateTime fecFin;

    @PrePersist
    protected void onCreate() {
        if (fecInicio == null) fecInicio = LocalDateTime.now();
    }
}