package com.firstech.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    // ─── Helpers ────────────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private Map<String, Object> bodyOf(ResponseEntity<Map<String, Object>> response) {
        return response.getBody();
    }

    // ─── Validation ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("MethodArgumentNotValidException → 400 com campo de erro no details")
    void handleValidation_returns400WithFieldErrors() throws NoSuchMethodException {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "obj");
        bindingResult.addError(new FieldError("obj", "email", "E-mail inválido"));

        // Obtém método qualquer para satisfazer o construtor
        var method = Object.class.getDeclaredMethod("toString");
        var ex = new MethodArgumentNotValidException(
                new org.springframework.core.MethodParameter(method, -1), bindingResult);

        ResponseEntity<Map<String, Object>> response = handler.handleValidation(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(bodyOf(response)).containsKey("details");

        @SuppressWarnings("unchecked")
        Map<String, String> details = (Map<String, String>) bodyOf(response).get("details");
        assertThat(details).containsEntry("email", "E-mail inválido");
    }

    // ─── EmailAlreadyExistsException ────────────────────────────────────────

    @Test
    @DisplayName("EmailAlreadyExistsException → 409")
    void handleEmailExists_returns409() {
        var ex = new EmailAlreadyExistsException("E-mail já está em uso.");
        var response = handler.handleEmailExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(bodyOf(response).get("message")).isEqualTo("E-mail já está em uso.");
    }

    // ─── InvalidTokenException ───────────────────────────────────────────────

    @Test
    @DisplayName("InvalidTokenException → 401")
    void handleInvalidToken_returns401() {
        var ex = new InvalidTokenException("Token expirado.");
        var response = handler.handleInvalidToken(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(bodyOf(response).get("message")).isEqualTo("Token expirado.");
    }

    // ─── ResourceNotFoundException ───────────────────────────────────────────

    @Test
    @DisplayName("ResourceNotFoundException → 404")
    void handleNotFound_returns404() {
        var ex = new ResourceNotFoundException("Recurso não encontrado.");
        var response = handler.handleNotFound(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    // ─── BadCredentialsException ─────────────────────────────────────────────

    @Test
    @DisplayName("BadCredentialsException → 401 com mensagem amigável")
    void handleBadCredentials_returns401WithFriendlyMessage() {
        var ex = new BadCredentialsException("bad");
        var response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(bodyOf(response).get("message"))
                .isEqualTo("E-mail ou senha incorretos.");
    }

    // ─── DisabledException ───────────────────────────────────────────────────

    @Test
    @DisplayName("DisabledException → 401 com mensagem de conta desativada")
    void handleDisabled_returns401WithDisabledMessage() {
        var ex = new DisabledException("disabled");
        var response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(bodyOf(response).get("message").toString())
                .contains("desativada");
    }

    // ─── Generic Exception ───────────────────────────────────────────────────

    @Test
    @DisplayName("Exception genérica → 500 com mensagem genérica")
    void handleGeneric_returns500() {
        var ex = new RuntimeException("unexpected");
        var response = handler.handleGeneric(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(bodyOf(response).get("message").toString())
                .contains("erro interno");
    }

    // ─── Response body structure ─────────────────────────────────────────────

    @Test
    @DisplayName("corpo da resposta deve conter timestamp, status, error e message")
    void responseBody_shouldContainRequiredFields() {
        var response = handler.handleNotFound(new ResourceNotFoundException("x"));
        var body = bodyOf(response);

        assertThat(body).containsKeys("timestamp", "status", "error", "message");
        assertThat(body.get("status")).isEqualTo(404);
    }
}
