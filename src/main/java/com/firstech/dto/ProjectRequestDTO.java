package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequestDTO(
        @NotBlank String name,
        String description,
        String thumbGradient,
        String icon,
        int orderIndex
) {}
