package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tra_modulo")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraModulo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_modulo")
    private Integer idModulo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_curso", nullable = false)
    private CatCurso curso;

    @Column(name = "des_nombre", length = 120, nullable = false)
    private String desNombre;

    @Column(name = "des_descripcion", columnDefinition = "TEXT")
    private String desDescripcion;

    @Column(name = "val_orden", nullable = false)
    private Short valOrden = 1;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;

    @Column(name = "fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fecCreacion;

    @PrePersist
    protected void onCreate() { fecCreacion = LocalDateTime.now(); }
}