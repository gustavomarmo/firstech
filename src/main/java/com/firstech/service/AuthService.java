package com.firstech.service;

import com.firstech.dto.*;
import com.firstech.exception.EmailAlreadyExistsException;
import com.firstech.exception.InvalidTokenException;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.PasswordResetToken;
import com.firstech.model.RefreshToken;
import com.firstech.model.User;
import com.firstech.model.Role;
import com.firstech.repository.PasswordResetTokenRepository;
import com.firstech.repository.RefreshTokenRepository;
import com.firstech.repository.UserRepository;
import com.firstech.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;

    @Value("${app.jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Value("${app.password-reset.expiration}")
    private long passwordResetExpiration;

    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("E-mail já está em uso.");
        }

        Role role = "RECRUTADOR".equalsIgnoreCase(request.roleType())
                ? Role.RECRUTADOR
                : Role.CANDIDATO;

        User user = User.builder()
                .name(request.name())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .roles(new HashSet<>(Set.of(role)))
                // Perfil comum
                .phone(request.phone())
                .linkedin(request.linkedin())
                .github(request.github())
                // Perfil talento
                .careerMoment(request.careerMoment())
                .workModel(request.workModel())
                .city(request.city())
                .tagline(request.tagline())
                .technologies(toList(request.technologies()))
                // Perfil recrutador
                .company(request.company())
                .companySize(request.companySize())
                .website(request.website())
                .cultureTags(toList(request.cultureTags()))
                .goals(toList(request.goals()))
                .build();

        userRepository.save(user);
        return buildAuthResponse(user);
    }

    private static <T> List<T> toList(List<T> source) {
        return source != null ? new ArrayList<>(source) : new ArrayList<>();
    }

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        return buildAuthResponse(user);
    }

    @Transactional
    public AuthResponseDTO refreshToken(RefreshTokenRequestDTO request) {
        RefreshToken stored = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token inválido."));

        if (stored.isExpired()) {
            refreshTokenRepository.delete(stored);
            throw new InvalidTokenException("Refresh token expirado. Faça login novamente.");
        }

        User user = stored.getUser();

        refreshTokenRepository.delete(stored);

        return buildAuthResponse(user);
    }

    @Transactional
    public void logout(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));
        refreshTokenRepository.deleteAllByUser(user);
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequestDTO request) {
        userRepository.findByEmail(request.email()).ifPresent(user -> {
            passwordResetTokenRepository.deleteByUser(user);

            String token = UUID.randomUUID().toString();
            PasswordResetToken resetToken = PasswordResetToken.builder()
                    .token(token)
                    .user(user)
                    .expiresAt(Instant.now().plusMillis(passwordResetExpiration))
                    .used(false)
                    .build();

            passwordResetTokenRepository.save(resetToken);
            emailService.sendPasswordResetEmail(user.getEmail(), user.getName(), token);
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequestDTO request) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new InvalidTokenException("Token inválido ou não encontrado."));

        if (resetToken.isUsed()) {
            throw new InvalidTokenException("Este token já foi utilizado.");
        }
        if (resetToken.isExpired()) {
            throw new InvalidTokenException("Token expirado. Solicite um novo link.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
        refreshTokenRepository.deleteAllByUser(user);
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user);
        String refreshToken = createRefreshToken(user);

        Set<String> roles = user.getRoles().stream()
                .map(Enum::name)
                .collect(Collectors.toSet());

        UserInfo userInfo = new UserInfo(user.getId(), user.getName(), user.getEmail(), roles);

        return AuthResponseDTO.of(accessToken, refreshToken, jwtUtil.getAccessTokenExpiration(), userInfo);
    }

    private String createRefreshToken(User user) {
        String tokenValue = UUID.randomUUID().toString();

        RefreshToken token = RefreshToken.builder()
                .token(tokenValue)
                .user(user)
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiration))
                .build();

        refreshTokenRepository.save(token);
        return tokenValue;
    }

}
