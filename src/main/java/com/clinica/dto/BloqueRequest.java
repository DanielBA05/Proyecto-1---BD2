package com.clinica.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BloqueRequest {
    @NotNull
    private LocalDateTime inicioBloque;
    @NotNull
    private LocalDateTime finBloque;
}
