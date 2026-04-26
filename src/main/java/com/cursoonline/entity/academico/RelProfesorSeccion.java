package com.cursoonline.entity.academico;

import com.cursoonline.entity.auth.SegUsuario;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "rel_profesor_seccion")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RelProfesorSeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_profesor_seccion")
    private Integer idProfesorSeccion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_usuario", nullable = false)
    private SegUsuario profesor;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "id_seccion", nullable = false)
    private TraSeccion seccion;

    @Column(name = "est_activo", nullable = false)
    private Boolean estActivo = true;

    @Column(name = "fec_asignacion", nullable = false, updatable = false)
    private LocalDateTime fecAsignacion;

    @PrePersist
    protected void onCreate() { fecAsignacion = LocalDateTime.now(); }
}