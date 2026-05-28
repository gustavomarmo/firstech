package com.firstech.dto;

/** Resumo de vaga para exibição no perfil público de recrutadores. */
public record VagaResumoDTO(
        Long   id,
        String titulo,
        String empresa,
        String companyLogo,   // CSS background (gradiente ou url)
        String companyLogoUrl,
        String modalidade,
        String nivel,
        String localizacao,
        boolean ativa,
        String applyUrl
) {}
