package com.firstech.controller;

import com.firstech.dto.AdminUserDTO;
import com.firstech.dto.AdminUserUpdateDTO;
import com.firstech.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST endpoints para gestão de usuários pelo administrador.
 * Protegido por {@code hasAuthority("ADMINISTRADOR")} no SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserApiController {

    private final AdminService adminService;

    /** Lista todos os usuários da plataforma. */
    @GetMapping
    public ResponseEntity<List<AdminUserDTO>> listUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    /** Atualiza nome, e-mail e/ou role de um usuário. */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @RequestBody AdminUserUpdateDTO dto,
            @AuthenticationPrincipal UserDetails currentUser) {
        try {
            AdminUserDTO updated = adminService.updateUser(id, dto);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /** Ativa ou desativa um usuário (toggle). */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            adminService.toggleUserStatus(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Exclui permanentemente um usuário e todos os seus dados. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        // Impede que o admin se exclua
        if (currentUser != null && currentUser.getUsername() != null) {
            try {
                // busca o email do usuário alvo para garantir que não é o próprio admin
                List<AdminUserDTO> all = adminService.getAllUsers();
                boolean isSelf = all.stream()
                        .filter(u -> u.id().equals(id))
                        .anyMatch(u -> u.email().equalsIgnoreCase(currentUser.getUsername()));
                if (isSelf) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Você não pode excluir sua própria conta."));
                }
            } catch (Exception ignored) { /* segue */ }
        }

        try {
            adminService.deleteUser(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
