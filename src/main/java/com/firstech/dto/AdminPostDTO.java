package com.firstech.dto;

import java.util.List;

/**
 * Snapshot de um post para exibição no painel administrativo.
 */
public record AdminPostDTO(
        Long         id,
        Long         authorId,
        String       authorNome,
        String       authorEmail,
        String       authorRole,      // "CANDIDATO" | "RECRUTADOR" | "ADMINISTRADOR"
        String       titulo,          // pode ser null
        String       conteudo,        // truncado em 140 chars
        List<String> tags,
        long         likes,
        int          comentarios,
        String       criadoEm        // dd/MM/yyyy HH:mm
) {}
