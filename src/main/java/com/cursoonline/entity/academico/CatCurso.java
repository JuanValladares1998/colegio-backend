package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cat_curso")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CatCurso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_curso")
    private Integer idCurso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_nivel")
    private CatNivel nivel;

    @Column(name = "des_nombre", length = 120, nullable = false)
    private String desNombre;

    @Column(name = "des_descripcion", columnDefinition = "TEXT")
    private String desDescripcion;

    @Column(name = "est_publicado", nullable = false)
    private Boolean estPublicado = false;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;

    @Column(name = "fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fecCreacion;

    @Column(name = "fec_publicacion")
    private LocalDateTime fecPublicacion;

    @PrePersist
    protected void onCreate() { fecCreacion = LocalDateTime.now(); }
}