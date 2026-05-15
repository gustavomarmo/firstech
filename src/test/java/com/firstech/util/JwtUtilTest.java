package com.firstech.util;

import com.firstech.model.Role;
import com.firstech.model.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JwtUtil")
class JwtUtilTest {

    // Mínimo 256 bits (32 chars) para HMAC-SHA256
    private static final String SECRET =
            "TestSecretKeyWith256BitsMinimumForHmacSha256Algorithm!!";
    private static final long EXPIRATION_MS = 900_000L; // 15 min

    private JwtUtil jwtUtil;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, EXPIRATION_MS);

        user = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encodedPassword")
                .roles(Set.of(Role.CANDIDATO))
                .build();
    }

    // ─── generateAccessToken ────────────────────────────────────────────────

    @Test
    @DisplayName("deve gerar token não nulo e não vazio")
    void generateAccessToken_shouldReturnNonBlankToken() {
        String token = jwtUtil.generateAccessToken(user);
        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("deve incluir o e-mail como subject")
    void generateAccessToken_shouldContainEmailAsSubject() {
        String token = jwtUtil.generateAccessToken(user);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("test@example.com");
    }

    @Test
    @DisplayName("deve incluir claims userId, name e roles")
    void generateAccessToken_shouldContainExpectedClaims() {
        user = User.builder()
                .id(42L)
                .name("Bianca")
                .email("bianca@example.com")
                .password("pw")
                .roles(Set.of(Role.CANDIDATO, Role.ADMINISTRADOR))
                .build();

        String token = jwtUtil.generateAccessToken(user);
        Claims claims = jwtUtil.extractAllClaims(token);

        assertThat(claims.get("userId", Integer.class)).isEqualTo(42);
        assertThat(claims.get("name", String.class)).isEqualTo("Bianca");

        String roles = claims.get("roles", String.class);
        assertThat(roles).contains("CANDIDATO");
        assertThat(roles).contains("ADMINISTRADOR");
    }

    // ─── isTokenValid ───────────────────────────────────────────────────────

    @Test
    @DisplayName("token recém-gerado deve ser válido")
    void isTokenValid_freshToken_shouldBeTrue() {
        String token = jwtUtil.generateAccessToken(user);
        assertThat(jwtUtil.isTokenValid(token)).isTrue();
    }

    @Test
    @DisplayName("token expirado deve ser inválido")
    void isTokenValid_expiredToken_shouldBeFalse() {
        // cria utilitário com expiração de -1 ms (já expirado)
        JwtUtil expiredUtil = new JwtUtil(SECRET, -1L);
        String token = expiredUtil.generateAccessToken(user);
        assertThat(expiredUtil.isTokenValid(token)).isFalse();
    }

    @Test
    @DisplayName("string aleatória não deve ser token válido")
    void isTokenValid_randomString_shouldBeFalse() {
        assertThat(jwtUtil.isTokenValid("not.a.jwt")).isFalse();
    }

    @Test
    @DisplayName("token vazio não deve ser válido")
    void isTokenValid_emptyString_shouldBeFalse() {
        assertThat(jwtUtil.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("token assinado com secret diferente não deve ser válido")
    void isTokenValid_differentSecret_shouldBeFalse() {
        JwtUtil otherUtil = new JwtUtil(
                "OutroSecretKeyWith256BitsMinimumForHmacSha256Algorithm!!", EXPIRATION_MS);
        String token = otherUtil.generateAccessToken(user);
        assertThat(jwtUtil.isTokenValid(token)).isFalse();
    }

    // ─── extractEmail ───────────────────────────────────────────────────────

    @Test
    @DisplayName("deve extrair o e-mail correto do token")
    void extractEmail_shouldReturnCorrectEmail() {
        String token = jwtUtil.generateAccessToken(user);
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("test@example.com");
    }

    // ─── getAccessTokenExpiration ───────────────────────────────────────────

    @Test
    @DisplayName("deve retornar o valor de expiração configurado")
    void getAccessTokenExpiration_shouldReturnConfiguredValue() {
        assertThat(jwtUtil.getAccessTokenExpiration()).isEqualTo(EXPIRATION_MS);
    }
}
