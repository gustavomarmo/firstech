package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record JobRequestDTO(
        @NotBlank(message = "Título é obrigatório")
        String title,

        String description,
        String location,
        String modality,   // "Remoto" | "Híbrido" | "Presencial"
        String level,      // "Estágio" | "Júnior" | "Pleno"
        String salary,
        List<String> tags,
        String jobCompany,
        String jobCompanyLogoUrl
) {}
