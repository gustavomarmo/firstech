package com.firstech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Payload enviado após o registro, contendo os dados extras do
 * Progressive Profiling (passo 2 em diante).
 *
 * O front-end chama POST /api/profile/complete com o access-token
 * já obtido em /api/auth/register.
 */
public record OnboardingProfileDTO(

        /** "CANDIDATO" ou "RECRUTADOR" */
        String roleType,

        // ── Campos compartilhados ──────────────────────────
        String phone,           // pendente de verificação

        // ── Candidato ──────────────────────────────────────
        String linkedin,
        String github,
        String careerMoment,    // "estagio" | "junior" | "studying"
        String workModel,       // "remote"  | "hybrid"  | "onsite"
        String city,
        String tagline,
        List<String> technologies,

        // ── Recrutador ─────────────────────────────────────
        String company,
        String companySize,
        String website,
        List<String> cultureTags,
        List<String> goals
) {}
