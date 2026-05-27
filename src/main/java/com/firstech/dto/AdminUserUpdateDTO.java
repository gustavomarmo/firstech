package com.firstech.dto;

/**
 * Payload para atualização de um usuário pelo administrador.
 */
public record AdminUserUpdateDTO(
        String nome,
        String email,
        String role    // "CANDIDATO" | "RECRUTADOR" | "ADMINISTRADOR"
) {}
