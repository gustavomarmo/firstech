package com.firstech.dto;
public record MessageDTO(
    Long id,
    Long senderId,
    String content,
    String time,
    boolean mine,
    boolean read
) {}
