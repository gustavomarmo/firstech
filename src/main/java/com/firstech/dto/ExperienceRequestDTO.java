package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;

public record ExperienceRequestDTO(
        @NotBlank String title,
        @NotBlank String companyName,
        String period,
        String description,
        int orderIndex
) {}
