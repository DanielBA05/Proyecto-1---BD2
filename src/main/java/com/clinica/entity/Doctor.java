package com.clinica.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DOCTOR")
@Getter
@Setter
public class Doctor {
    @Id
    @Column(name = "ID_DOCTOR")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "ID_DOCTOR")
    private Usuario usuario;

    @Column(name = "CODIGO_PROFESIONAL", length = 50)
    private String codigoProfesional;

    @Column(name = "ESPECIALIDAD", length = 100)
    private String especialidad;
}
