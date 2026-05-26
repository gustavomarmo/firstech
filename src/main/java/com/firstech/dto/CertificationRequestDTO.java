package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;

public record CertificationRequestDTO(
        String emoji,
        @NotBlank String name,
        String issuer,
        String issueDate,
        int orderIndex
) {}
