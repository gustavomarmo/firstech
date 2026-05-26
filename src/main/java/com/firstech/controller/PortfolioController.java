package com.firstech.controller;

import com.firstech.dto.*;
import com.firstech.model.User;
import com.firstech.service.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/portfolio")
@RequiredArgsConstructor
public class PortfolioController {

    private final PortfolioService portfolioService;

    // ── Avatar ─────────────────────────────────────────────────────────

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal User user) throws IOException {
        portfolioService.updateAvatar(file, user);
        return ResponseEntity.noContent().build();
    }

    // ── Perfil ─────────────────────────────────────────────────────────

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(
            @RequestBody ProfileUpdateDTO dto,
            @AuthenticationPrincipal User user) {
        portfolioService.updateProfile(dto, user);
        return ResponseEntity.noContent().build();
    }

    // ── Experiências ────────────────────────────────────────────────────

    @PostMapping("/experiences")
    public ResponseEntity<ExperienciaViewModel> addExperience(
            @Valid @RequestBody ExperienceRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portfolioService.addExperience(dto, user));
    }

    @PutMapping("/experiences/{id}")
    public ResponseEntity<ExperienciaViewModel> updateExperience(
            @PathVariable Long id,
            @Valid @RequestBody ExperienceRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(portfolioService.updateExperience(id, dto, user));
    }

    @DeleteMapping("/experiences/{id}")
    public ResponseEntity<Void> deleteExperience(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        portfolioService.deleteExperience(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── Habilidades ─────────────────────────────────────────────────────

    @PostMapping("/skills")
    public ResponseEntity<HabilidadeViewModel> addSkill(
            @Valid @RequestBody SkillRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portfolioService.addSkill(dto, user));
    }

    @PutMapping("/skills/{id}")
    public ResponseEntity<HabilidadeViewModel> updateSkill(
            @PathVariable Long id,
            @Valid @RequestBody SkillRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(portfolioService.updateSkill(id, dto, user));
    }

    @DeleteMapping("/skills/{id}")
    public ResponseEntity<Void> deleteSkill(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        portfolioService.deleteSkill(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── Certificações ───────────────────────────────────────────────────

    @PostMapping("/certifications")
    public ResponseEntity<CertificacaoViewModel> addCertification(
            @Valid @RequestBody CertificationRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portfolioService.addCertification(dto, user));
    }

    @PutMapping("/certifications/{id}")
    public ResponseEntity<CertificacaoViewModel> updateCertification(
            @PathVariable Long id,
            @Valid @RequestBody CertificationRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(portfolioService.updateCertification(id, dto, user));
    }

    @DeleteMapping("/certifications/{id}")
    public ResponseEntity<Void> deleteCertification(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        portfolioService.deleteCertification(id, user);
        return ResponseEntity.noContent().build();
    }

    // ── Projetos ────────────────────────────────────────────────────────

    @PostMapping("/projects")
    public ResponseEntity<ProjetoViewModel> addProject(
            @Valid @RequestBody ProjectRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(portfolioService.addProject(dto, user));
    }

    @PutMapping("/projects/{id}")
    public ResponseEntity<ProjetoViewModel> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody ProjectRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(portfolioService.updateProject(id, dto, user));
    }

    @DeleteMapping("/projects/{id}")
    public ResponseEntity<Void> deleteProject(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        portfolioService.deleteProject(id, user);
        return ResponseEntity.noContent().build();
    }
}
