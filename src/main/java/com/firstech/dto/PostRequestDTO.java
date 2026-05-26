package com.firstech.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record PostRequestDTO(
        @NotBlank(message = "Conteúdo é obrigatório")
        String content,

        String title,
        List<String> tags,
        Long linkedJobId
) {}
