package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;

public record ProjectRequestDTO(
        @NotBlank String name,
        String description,
        String thumbGradient,      // mantido para compatibilidade; pode ser null
        String icon,               // mantido para compatibilidade; pode ser null
        String thumbImageBase64,   // imagem de thumbnail em base64 (nova)
        String githubUrl,          // link do repositório no GitHub (novo)
        int    orderIndex
) {}
