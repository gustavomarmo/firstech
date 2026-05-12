package com.firstech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name,
        @NotBlank @Email(message = "E-mail inválido")
        String email,
        @NotBlank @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String password
) {}
