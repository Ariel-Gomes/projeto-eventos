package com.exemplo.eventos.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record InscricaoDto(
        @NotNull Long eventoId,
        @NotBlank String nomeParticipante,
        @NotBlank @Email String email
) {}