package com.firstech.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record RegisterRequestDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name,

        @NotBlank @Email(message = "E-mail inválido")
        String email,

        @NotBlank @Size(min = 8, message = "Senha deve ter no mínimo 8 caracteres")
        String password,

        String roleType,        // "CANDIDATO" | "RECRUTADOR"

        // ── Perfil comum ──────────────────────────────────────
        String phone,
        String linkedin,
        String github,

        // ── Perfil talento ────────────────────────────────────
        String careerMoment,    // "estagio" | "junior" | "studying"
        String workModel,       // "remote" | "hybrid" | "onsite"
        String city,
        String tagline,
        List<String> technologies,

        // ── Perfil recrutador ─────────────────────────────────
        String company,
        String companySize,
        String website,
        List<String> cultureTags,
        List<String> goals
) {}
