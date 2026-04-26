package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tra_leccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraLeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_leccion")
    private Integer idLeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_modulo", nullable = false)
    private TraModulo modulo;

    @Column(name = "des_nombre", length = 150, nullable = false)
    private String desNombre;

    @Column(name = "des_contenido", columnDefinition = "TEXT")
    private String desContenido;

    @Column(name = "val_orden", nullable = false)
    private Short valOrden = 1;

    @Column(name = "est_obligatoria", nullable = false)
    private Boolean estObligatoria = true;

    @Column(name = "est_publicada", nullable = false)
    private Boolean estPublicada = false;

    @Column(name = "est_activa", nullable = false)
    private Boolean estActiva = true;

    @Column(name = "fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fecCreacion;

    @Column(name = "fec_publicacion")
    private LocalDateTime fecPublicacion;

    @PrePersist
    protected void onCreate() { fecCreacion = LocalDateTime.now(); }
}