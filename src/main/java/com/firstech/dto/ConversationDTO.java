package com.firstech.dto;
public record ConversationDTO(
    Long userId,
    String nome,
    String inicial,
    String corAvatar,
    String avatarBase64,
    String lastMessage,
    String lastMessageTime,
    int unreadCount
) {}
