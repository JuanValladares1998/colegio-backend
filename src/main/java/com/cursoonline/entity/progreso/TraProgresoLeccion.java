package com.cursoonline.entity.progreso;

import com.cursoonline.entity.academico.TraLeccion;
import com.cursoonline.entity.auth.SegUsuario;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tra_progreso_leccion",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_progreso_usuario_leccion",
           columnNames = {"id_usuario", "id_leccion"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraProgresoLeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_progreso")
    private Integer idProgreso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario", nullable = false)
    private SegUsuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_leccion", nullable = false)
    private TraLeccion leccion;

    @Column(name = "est_completada", nullable = false)
    private Boolean estCompletada = false;

    @Column(name = "fec_inicio", nullable = false)
    private LocalDateTime fecInicio;

    @Column(name = "fec_completado")
    private LocalDateTime fecCompletado;

    @PrePersist
    protected void onCreate() {
        if (fecInicio == null) fecInicio = LocalDateTime.now();
    }
}