package com.clinica.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CitaRequest {
    @NotNull
    private Long bloqueId;

    @NotNull
    private LocalDateTime inicioCita;

    @NotNull
    private LocalDateTime finCita;
}