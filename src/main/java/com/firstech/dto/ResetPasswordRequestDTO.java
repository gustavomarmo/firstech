package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ResetPasswordRequestDTO(
        @NotBlank
        String token,
        @NotBlank @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String newPassword
) {}
