package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tra_recurso")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraRecurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_recurso")
    private Integer idRecurso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_leccion", nullable = false)
    private TraLeccion leccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_tipo_recurso", nullable = false)
    private CatTipoRecurso tipoRecurso;

    @Column(name = "des_nombre", length = 150, nullable = false)
    private String desNombre;

    @Column(name = "url_ruta", columnDefinition = "TEXT", nullable = false)
    private String urlRuta;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;

    @Column(name = "fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fecCreacion;

    @PrePersist
    protected void onCreate() { fecCreacion = LocalDateTime.now(); }
}