package com.cursoonline.entity.evaluacion;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tra_respuesta_alumno")
@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class TraRespuestaAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_respuesta_alumno")
    private Integer idRespuestaAlumno;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_intento", nullable = false)
    private TraIntentoEvaluacion intento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_pregunta", nullable = false)
    private TraPregunta pregunta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_opcion_elegida")
    private TraOpcionRespuesta opcionElegida;

    @Column(name = "des_respuesta_texto", columnDefinition = "TEXT")
    private String desRespuestaTexto;

    @Column(name = "est_correcta")
    private Boolean estCorrecta;

    @Column(name = "fec_respuesta", nullable = false)
    private LocalDateTime fecRespuesta;

    @PrePersist
    protected void onCreate() {
        if (fecRespuesta == null) fecRespuesta = LocalDateTime.now();
    }
}