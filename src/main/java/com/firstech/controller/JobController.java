package com.firstech.controller;

import com.firstech.dto.JobRequestDTO;
import com.firstech.dto.JobResponseDTO;
import com.firstech.model.User;
import com.firstech.service.JobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    /** Todas as vagas ativas — público (candidatos e visitantes podem ver). */
    @GetMapping
    public ResponseEntity<List<JobResponseDTO>> getActiveJobs() {
        return ResponseEntity.ok(jobService.getActiveJobs());
    }

    /** Vagas publicadas pelo recrutador autenticado. */
    @GetMapping("/minhas")
    @PreAuthorize("hasAuthority('RECRUTADOR')")
    public ResponseEntity<List<JobResponseDTO>> getMyJobs(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobService.getRecruiterJobs(user));
    }

    /** Publica uma nova vaga. */
    @PostMapping
    @PreAuthorize("hasAuthority('RECRUTADOR')")
    public ResponseEntity<JobResponseDTO> createJob(
            @Valid @RequestBody JobRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(HttpStatus.CREATED).body(jobService.createJob(dto, user));
    }

    /** Atualiza os dados de uma vaga existente. */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUTADOR')")
    public ResponseEntity<JobResponseDTO> updateJob(
            @PathVariable Long id,
            @Valid @RequestBody JobRequestDTO dto,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(jobService.updateJob(id, dto, user));
    }

    /** Encerra uma vaga (muda status para ENCERRADA). */
    @PatchMapping("/{id}/fechar")
    @PreAuthorize("hasAuthority('RECRUTADOR')")
    public ResponseEntity<Void> closeJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        jobService.closeJob(id, user);
        return ResponseEntity.noContent().build();
    }

    /** Exclui permanentemente uma vaga. */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('RECRUTADOR')")
    public ResponseEntity<Void> deleteJob(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        jobService.deleteJob(id, user);
        return ResponseEntity.noContent().build();
    }
}
