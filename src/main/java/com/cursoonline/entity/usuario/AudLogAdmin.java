package com.cursoonline.entity.usuario;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

import com.cursoonline.entity.auth.SegUsuario;

@Entity
@Table(name = "aud_log_admin")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AudLogAdmin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_log_admin")
    private Integer idLogAdmin;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_administrador", nullable = false)
    private SegUsuario administrador;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_usuario_afectado")
    private SegUsuario usuarioAfectado;

    // CREAR_USUARIO | CAMBIO_ROL | CAMBIO_ESTADO | RECUPERAR_CREDENCIALES | CARGA_MASIVA
    @Column(name = "des_accion", length = 100, nullable = false)
    private String desAccion;

    @Column(name = "des_detalle", columnDefinition = "TEXT")
    private String desDetalle;

    @Column(name = "fec_accion", nullable = false)
    private LocalDateTime fecAccion;

    @PrePersist
    protected void onCreate() { fecAccion = LocalDateTime.now(); }
}