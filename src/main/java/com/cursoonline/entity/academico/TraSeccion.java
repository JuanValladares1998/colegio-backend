package com.cursoonline.entity.academico;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "tra_seccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seccion")
    private Integer idSeccion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_curso", nullable = false)
    private CatCurso curso;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_anio_escolar", nullable = false)
    private CatAnioEscolar anioEscolar;

    @Column(name = "des_nombre", length = 80, nullable = false)
    private String desNombre;

    @Column(name = "est_activa", nullable = false)
    private Boolean estActiva = true;

    @Column(name = "fec_creacion", nullable = false, updatable = false)
    private LocalDateTime fecCreacion;

    @PrePersist
    protected void onCreate() { fecCreacion = LocalDateTime.now(); }
}