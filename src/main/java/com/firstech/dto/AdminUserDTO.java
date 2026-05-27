package com.firstech.dto;

/**
 * Snapshot de um usuário para exibição no painel administrativo.
 */
public record AdminUserDTO(
        Long    id,
        String  nome,
        String  email,
        String  role,        // "CANDIDATO" | "RECRUTADOR" | "ADMINISTRADOR"
        String  cargo,       // label legível derivado da role / careerMoment
        String  empresa,     // preenchido só para recrutadores
        String  cidade,
        boolean ativo,
        String  criadoEm,    // LocalDate formatada como dd/MM/yyyy
        long    totalPosts,
        int     totalConexoes
) {}
