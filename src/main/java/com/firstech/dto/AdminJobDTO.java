package com.firstech.dto;

import java.util.List;

/**
 * Snapshot de uma vaga para exibição no painel administrativo.
 */
public record AdminJobDTO(
        Long         id,
        String       titulo,
        String       empresa,          // jobCompany ou company do recrutador
        Long         recrutadorId,
        String       recrutadorNome,
        String       recrutadorEmail,
        String       modalidade,       // "Remoto" | "Híbrido" | "Presencial"
        String       nivel,            // "Estágio" | "Júnior" | "Pleno" | "Sênior"
        String       salario,
        String       localizacao,
        String       status,           // "ATIVA" | "ENCERRADA"
        List<String> tags,
        String       criadoEm         // dd/MM/yyyy
) {}
