package com.firstech.dto;

public record AuthResponseDTO(
        String accessToken,
        String refreshToken,
        String tokenType,
        long expiresIn,
        UserInfo user
) {
    public static AuthResponseDTO of(String accessToken, String refreshToken,
                                  long expiresIn, UserInfo user) {
        return new AuthResponseDTO(accessToken, refreshToken, "Bearer", expiresIn, user);
    }
}
