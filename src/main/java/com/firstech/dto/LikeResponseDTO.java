package com.firstech.dto;

/**
 * Resposta do toggle de curtida em um post.
 */
public record LikeResponseDTO(boolean liked, int count) {}
