package com.clinica.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RegisterPacienteRequest {
    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    @Size(min = 6, max = 100)
    private String password;

    @NotBlank
    private String confirmPassword;

    @NotBlank
    @Email
    @Size(max = 100)
    private String correo;

    @Size(max = 20)
    private String telefono;

    @NotBlank
    @Size(max = 50)
    private String nombre;

    @NotBlank
    @Size(max = 50)
    private String apellido1;

    @Size(max = 50)
    private String apellido2;

    @Past
    private LocalDate fechaNacimiento;

    @Size(max = 10)
    private String sexo;

    @Size(max = 150)
    private String direccion;
}
