package com.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "CONSULTORIO")
@Getter
@Setter
public class Consultorio {
    @Id
    @Column(name = "ID_CONSULTORIO")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "consultorio_seq_gen")
    @SequenceGenerator(name = "consultorio_seq_gen", sequenceName = "SEQ_CONSULTORIO", allocationSize = 1)
    private Long id;

    @Column(name = "NOMBRE", nullable = false, unique = true, length = 50)
    private String nombre;

    @Column(name = "UBICACION", nullable = false, length = 100)
    private String ubicacion;
}
