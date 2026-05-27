package com.firstech.controller;

import com.firstech.dto.AdminJobDTO;
import com.firstech.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * REST endpoints para gestão de vagas pelo administrador.
 * Protegido por {@code hasAuthority("ADMINISTRADOR")} no SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/jobs")
@RequiredArgsConstructor
public class AdminJobApiController {

    private final AdminService adminService;

    /** Lista todas as vagas da plataforma. */
    @GetMapping
    public ResponseEntity<List<AdminJobDTO>> listJobs() {
        return ResponseEntity.ok(adminService.getAllJobs());
    }

    /** Alterna o status de uma vaga (ATIVA ↔ ENCERRADA). */
    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        try {
            AdminJobDTO updated = adminService.toggleJobStatus(id);
            return ResponseEntity.ok(updated);
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** Exclui permanentemente uma vaga. */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteJob(@PathVariable Long id) {
        try {
            adminService.deleteJob(id);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (NoSuchElementException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
