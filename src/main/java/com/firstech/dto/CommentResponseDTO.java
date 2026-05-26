package com.firstech.dto;

/**
 * Comentário de post para exibição no front-end.
 */
public record CommentResponseDTO(
        Long   id,
        String content,
        String autorNome,
        String autorInicial,
        String autorCorAvatar,
        String autorAvatarBase64,   // null quando não há foto
        String tempoRelativo
) {}
