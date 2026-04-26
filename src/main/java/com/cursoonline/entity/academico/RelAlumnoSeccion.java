package com.cursoonline.entity.academico;

import com.cursoonline.entity.auth.SegUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "rel_alumno_seccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelAlumnoSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_alumno_seccion")
    private Integer idAlumnoSeccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private SegUsuario alumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_seccion", nullable = false)
    private TraSeccion seccion;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;

    @Column(name = "fec_inscripcion", nullable = false, updatable = false)
    private LocalDateTime fecInscripcion;

    @PrePersist
    protected void onCreate() { fecInscripcion = LocalDateTime.now(); }
}