package com.firstech.controller;

import com.firstech.dto.MessageResponseDTO;
import com.firstech.dto.OnboardingProfileDTO;
import com.firstech.model.User;
import com.firstech.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

/**
 * Endpoints de perfil do usuário.
 *
 * POST /api/profile/complete
 *   Recebe os dados extras coletados no Progressive Profiling
 *   (habilidades, empresa, cultura, etc.) e os persiste.
 *   Requer JWT válido no header Authorization: Bearer <token>.
 */
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    /**
     * Chamado pelo front-end logo após o registro, enviando os dados
     * dos passos 2–4 do onboarding.
     *
     * O token JWT já foi salvo no localStorage pelo onboarding.js e
     * deve ser enviado no header:  Authorization: Bearer <accessToken>
     */
    @PostMapping("/complete")
    public ResponseEntity<MessageResponseDTO> completeProfile(
            @RequestBody OnboardingProfileDTO dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!(userDetails instanceof User user)) {
            return ResponseEntity.status(401)
                    .body(new MessageResponseDTO("Não autorizado."));
        }

        profileService.saveOnboardingProfile(user, dto);
        return ResponseEntity.ok(new MessageResponseDTO("Perfil atualizado com sucesso."));
    }
}
