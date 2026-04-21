package com.clinica.entity;

import com.clinica.enums.EstadoBloque;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "BLOQUE_HORARIO")
@Getter
@Setter
public class BloqueHorario {
    @Id
    @Column(name = "ID_BLOQUE")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "bloque_seq_gen")
    @SequenceGenerator(name = "bloque_seq_gen", sequenceName = "SEQ_BLOQUE_HORARIO", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_DOCTOR", nullable = false)
    private Doctor doctor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CONSULTORIO", nullable = false)
    private Consultorio consultorio;

    @Column(name = "INICIO_BLOQUE", nullable = false)
    private LocalDateTime inicioBloque;

    @Column(name = "FIN_BLOQUE", nullable = false)
    private LocalDateTime finBloque;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_BLOQUE", nullable = false, length = 20)
    private EstadoBloque estadoBloque;
}
