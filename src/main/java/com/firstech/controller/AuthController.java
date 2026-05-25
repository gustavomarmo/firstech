package com.firstech.controller;

import com.firstech.dto.*;
import com.firstech.model.User;
import com.firstech.service.AuthService;
import com.firstech.util.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request,
                                                    HttpServletResponse response) {
        AuthResponseDTO data = authService.register(request);
        addJwtCookie(response, data.accessToken());
        return ResponseEntity.status(HttpStatus.CREATED).body(data);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request,
                                                 HttpServletResponse response) {
        AuthResponseDTO data = authService.login(request);
        addJwtCookie(response, data.accessToken());
        return ResponseEntity.ok(data);
    }

    private void addJwtCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (jwtUtil.getAccessTokenExpiration() / 1000));
        response.addCookie(cookie);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponseDTO> refresh(@Valid @RequestBody RefreshTokenRequestDTO request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponseDTO> logout(@AuthenticationPrincipal UserDetails userDetails) {
        authService.logout(userDetails.getUsername());
        return ResponseEntity.ok(new MessageResponseDTO("Logout realizado com sucesso."));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponseDTO> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequestDTO request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok(new MessageResponseDTO(
                "Se o e-mail estiver cadastrado, você receberá as instruções em breve."));
    }

    /**
     * POST /api/auth/reset-password
     * Redefine a senha usando o token recebido por e-mail.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponseDTO> resetPassword(
            @Valid @RequestBody ResetPasswordRequestDTO request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(new MessageResponseDTO("Senha redefinida com sucesso."));
    }

    /**
     * GET /api/auth/me
     * Retorna os dados do usuário autenticado.
     */
    @GetMapping("/me")
    public ResponseEntity<UserInfo> me(@AuthenticationPrincipal UserDetails userDetails) {
        // Os dados vêm direto do Principal (sem bater no banco novamente)
        if (userDetails instanceof User user) {
            var roles = user.getRoles().stream()
                    .map(Enum::name)
                    .collect(java.util.stream.Collectors.toSet());
            return ResponseEntity.ok(new UserInfo(user.getId(), user.getName(), user.getEmail(), roles));
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

}
