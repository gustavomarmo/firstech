package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;

public record SkillRequestDTO(
        @NotBlank String name,
        boolean highlight,
        int orderIndex
) {}
