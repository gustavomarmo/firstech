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

    @SuppressWarnings("unchecked")
    private Map<String, Object> bodyOf(ResponseEntity<Map<String, Object>> response) {
        return response.getBody();
    }

    @Test
    @DisplayName("EmailAlreadyExistsException → 409")
    void handleEmailExists_returns409() {
        var ex = new EmailAlreadyExistsException("E-mail já está em uso.");
        var response = handler.handleEmailExists(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(bodyOf(response).get("message")).isEqualTo("E-mail já está em uso.");
    }

    @Test
    @DisplayName("BadCredentialsException → 401 com mensagem amigável")
    void handleBadCredentials_returns401WithFriendlyMessage() {
        var ex = new BadCredentialsException("bad");
        var response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(bodyOf(response).get("message"))
                .isEqualTo("E-mail ou senha incorretos.");
    }

    @Test
    @DisplayName("DisabledException → 401 com mensagem de conta desativada")
    void handleDisabled_returns401WithDisabledMessage() {
        var ex = new DisabledException("disabled");
        var response = handler.handleBadCredentials(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(bodyOf(response).get("message").toString())
                .contains("desativada");
    }

    @Test
    @DisplayName("corpo da resposta deve conter timestamp, status, error e message")
    void responseBody_shouldContainRequiredFields() {
        var response = handler.handleNotFound(new ResourceNotFoundException("x"));
        var body = bodyOf(response);

        assertThat(body).containsKeys("timestamp", "status", "error", "message");
        assertThat(body.get("status")).isEqualTo(404);
    }
}
