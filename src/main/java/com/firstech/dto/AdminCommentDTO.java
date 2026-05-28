package com.firstech.dto;

/**
 * Snapshot de um comentário para o painel administrativo.
 */
public record AdminCommentDTO(
        Long   id,
        Long   postId,
        Long   authorId,
        String authorNome,
        String authorEmail,
        String authorRole,   // "CANDIDATO" | "RECRUTADOR" | "ADMINISTRADOR"
        String conteudo,
        String criadoEm      // dd/MM/yyyy HH:mm
) {}
