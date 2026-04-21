package com.clinica.entity;

import com.clinica.enums.EstadoCita;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "CITA")
@Getter
@Setter
public class Cita {
    @Id
    @Column(name = "ID_CITA")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "cita_seq_gen")
    @SequenceGenerator(name = "cita_seq_gen", sequenceName = "SEQ_CITA", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_BLOQUE", nullable = false)
    private BloqueHorario bloque;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_PACIENTE")
    private Paciente paciente;

    @Enumerated(EnumType.STRING)
    @Column(name = "ESTADO_CITA", nullable = false, length = 20)
    private EstadoCita estadoCita;

    @Column(name = "INICIO_CITA", nullable = false)
    private LocalDateTime inicioCita;

    @Column(name = "FIN_CITA", nullable = false)
    private LocalDateTime finCita;
}
