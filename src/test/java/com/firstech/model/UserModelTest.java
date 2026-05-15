package com.firstech.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Modelos de domínio")
class UserModelTest {

    @Nested
    @DisplayName("User")
    class ModelTest {

        private User buildUser(Set<Role> roles) {
            return User.builder()
                    .id(1L)
                    .name("Bianca")
                    .email("bianca@example.com")
                    .password("encodedPw")
                    .roles(roles)
                    .build();
        }

        @Test
        @DisplayName("role padrão via @Builder.Default deve ser CANDIDATO")
        void defaultRole_shouldBeCandidato() {
            User user = User.builder()
                    .id(2L).name("X").email("x@x.com").password("pw")
                    .build();
            assertThat(user.getRoles()).containsExactly(Role.CANDIDATO);
        }
    }

    @Nested
    @DisplayName("PasswordResetToken")
    class PasswordResetTokenModelTest {

        @Test
        @DisplayName("isExpired() deve retornar false para token ainda válido")
        void isExpired_freshToken_shouldBeFalse() {
            PasswordResetToken token = PasswordResetToken.builder()
                    .token("abc")
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
            assertThat(token.isExpired()).isFalse();
        }
    }

    @Nested
    @DisplayName("RefreshToken")
    class RefreshTokenModelTest {

        @Test
        @DisplayName("isExpired() deve retornar true para token vencido")
        void isExpired_expiredToken_shouldBeTrue() {
            RefreshToken token = RefreshToken.builder()
                    .token("rt")
                    .expiresAt(Instant.now().minusSeconds(1))
                    .build();
            assertThat(token.isExpired()).isTrue();
        }
    }
}
