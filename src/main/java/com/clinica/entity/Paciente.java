package com.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "PACIENTE")
@Getter
@Setter
public class Paciente {
    @Id
    @Column(name = "ID_PACIENTE")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ID_PACIENTE")
    private Usuario usuario;

    @Column(name = "FECHA_NACIMIENTO")
    private LocalDate fechaNacimiento;

    @Column(name = "SEXO", length = 10)
    private String sexo;

    @Column(name = "DIRECCION", length = 150)
    private String direccion;
}
