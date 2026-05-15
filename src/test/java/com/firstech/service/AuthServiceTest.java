package com.firstech.service;

import com.firstech.dto.*;
import com.firstech.exception.EmailAlreadyExistsException;
import com.firstech.exception.InvalidTokenException;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.PasswordResetToken;
import com.firstech.model.Role;
import com.firstech.model.User;
import com.firstech.repository.PasswordResetTokenRepository;
import com.firstech.repository.RefreshTokenRepository;
import com.firstech.repository.UserRepository;
import com.firstech.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService")
class AuthServiceTest {

    @Mock UserRepository               userRepository;
    @Mock RefreshTokenRepository       refreshTokenRepository;
    @Mock PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock JwtUtil                      jwtUtil;
    @Mock PasswordEncoder              passwordEncoder;
    @Mock AuthenticationManager        authenticationManager;
    @Mock
    EmailService emailService;

    @InjectMocks
    AuthService authService;

    private User defaultUser;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration",  604_800_000L);
        ReflectionTestUtils.setField(authService, "passwordResetExpiration", 3_600_000L);

        defaultUser = User.builder()
                .id(1L)
                .name("Test User")
                .email("test@example.com")
                .password("encodedPw")
                .roles(Set.of(Role.CANDIDATO))
                .build();
    }

    private void stubTokenGeneration() {
        when(jwtUtil.generateAccessToken(any())).thenReturn("access-token");
        when(jwtUtil.getAccessTokenExpiration()).thenReturn(900_000L);
        when(refreshTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("deve registrar usuário e retornar tokens")
        void success() {
            when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
            when(passwordEncoder.encode("rawPassword")).thenReturn("encodedPw");
            when(userRepository.save(any())).thenReturn(defaultUser);
            stubTokenGeneration();

            RegisterRequestDTO request = new RegisterRequestDTO("Test User", "new@example.com", "rawPassword");
            AuthResponseDTO response = authService.register(request);

            assertThat(response.accessToken()).isEqualTo("access-token");
            assertThat(response.tokenType()).isEqualTo("Bearer");
            verify(userRepository).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("login()")
    class Login {

        @Test
        @DisplayName("deve autenticar e retornar tokens")
        void success() {
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(defaultUser));
            stubTokenGeneration();

            AuthResponseDTO response = authService.login(
                    new LoginRequestDTO("test@example.com", "rawPw"));

            assertThat(response.user().email()).isEqualTo("test@example.com");
            verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        }
    }

    @Nested
    @DisplayName("refreshToken()")
    class RefreshToken {

        private com.firstech.model.RefreshToken buildToken(boolean expired) {
            return com.firstech.model.RefreshToken.builder()
                    .id(10L)
                    .token("refresh-abc")
                    .user(defaultUser)
                    .expiresAt(expired
                            ? Instant.now().minusSeconds(60)
                            : Instant.now().plusSeconds(3600))
                    .build();
        }

        @Test
        @DisplayName("deve emitir novos tokens com refresh token válido")
        void success() {
            when(refreshTokenRepository.findByToken("refresh-abc"))
                    .thenReturn(Optional.of(buildToken(false)));
            stubTokenGeneration();

            AuthResponseDTO response = authService.refreshToken(
                    new RefreshTokenRequestDTO("refresh-abc"));

            assertThat(response.accessToken()).isEqualTo("access-token");
            verify(refreshTokenRepository).delete(any());
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException e deletar token expirado")
        void tokenExpired() {
            com.firstech.model.RefreshToken expired = buildToken(true);
            when(refreshTokenRepository.findByToken("refresh-abc")).thenReturn(Optional.of(expired));

            assertThatThrownBy(() ->
                    authService.refreshToken(new RefreshTokenRequestDTO("refresh-abc"))
            ).isInstanceOf(InvalidTokenException.class)
             .hasMessageContaining("expirado");

            verify(refreshTokenRepository).delete(expired);
        }
    }

    @Nested
    @DisplayName("logout()")
    class Logout {

        @Test
        @DisplayName("deve deletar todos os refresh tokens do usuário")
        void success() {
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(defaultUser));

            authService.logout("test@example.com");

            verify(refreshTokenRepository).deleteAllByUser(defaultUser);
        }
    }

    @Nested
    @DisplayName("forgotPassword()")
    class ForgotPassword {

        @Test
        @DisplayName("deve criar token e enviar e-mail quando usuário existe")
        void userExists() {
            when(userRepository.findByEmail("test@example.com"))
                    .thenReturn(Optional.of(defaultUser));
            when(passwordResetTokenRepository.save(any()))
                    .thenAnswer(inv -> inv.getArgument(0));

            authService.forgotPassword(new ForgotPasswordRequestDTO("test@example.com"));

            verify(passwordResetTokenRepository).deleteByUser(defaultUser);
            verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
            verify(emailService).sendPasswordResetEmail(eq("test@example.com"), eq("Test User"), anyString());
        }
    }

    @Nested
    @DisplayName("resetPassword()")
    class ResetPassword {

        private PasswordResetToken buildResetToken(boolean used, boolean expired) {
            return PasswordResetToken.builder()
                    .id(5L)
                    .token("reset-token-xyz")
                    .user(defaultUser)
                    .used(used)
                    .expiresAt(expired
                            ? Instant.now().minusSeconds(60)
                            : Instant.now().plusSeconds(3600))
                    .build();
        }

        @Test
        @DisplayName("deve redefinir senha com token válido")
        void success() {
            PasswordResetToken valid = buildResetToken(false, false);
            when(passwordResetTokenRepository.findByToken("reset-token-xyz"))
                    .thenReturn(Optional.of(valid));
            when(passwordEncoder.encode("newSecret8")).thenReturn("encodedNew");
            when(userRepository.save(any())).thenReturn(defaultUser);
            when(passwordResetTokenRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            authService.resetPassword(new ResetPasswordRequestDTO("reset-token-xyz", "newSecret8"));

            assertThat(defaultUser.getPassword()).isEqualTo("encodedNew");
            assertThat(valid.isUsed()).isTrue();
            verify(refreshTokenRepository).deleteAllByUser(defaultUser);
        }

        @Test
        @DisplayName("deve lançar InvalidTokenException para token expirado")
        void tokenExpired() {
            when(passwordResetTokenRepository.findByToken("reset-token-xyz"))
                    .thenReturn(Optional.of(buildResetToken(false, true)));

            assertThatThrownBy(() ->
                    authService.resetPassword(new ResetPasswordRequestDTO("reset-token-xyz", "newSecret8"))
            ).isInstanceOf(InvalidTokenException.class)
             .hasMessageContaining("expirado");
        }
    }
}
