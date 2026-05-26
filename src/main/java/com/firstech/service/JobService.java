package com.firstech.service;

import com.firstech.dto.JobRequestDTO;
import com.firstech.dto.JobResponseDTO;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.Job;
import com.firstech.model.JobStatus;
import com.firstech.model.User;
import com.firstech.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;

    private static final String[] LOGO_GRADIENTS = {
            "linear-gradient(135deg,#6d28d9,#8b5cf6)",
            "linear-gradient(135deg,#1a56db,#3a86ff)",
            "linear-gradient(135deg,#0f6e56,#1d9e75)",
            "linear-gradient(135deg,#854f0b,#ef9f27)",
            "linear-gradient(135deg,#0e7490,#06b6d4)",
            "linear-gradient(135deg,#be185d,#f472b6)",
            "linear-gradient(135deg,#9a3412,#fb923c)",
    };

    @Transactional
    public JobResponseDTO createJob(JobRequestDTO dto, User recruiter) {
        Job job = Job.builder()
                .title(dto.title())
                .description(dto.description())
                .location(dto.location())
                .modality(dto.modality())
                .level(dto.level())
                .salary(dto.salary())
                .tags(dto.tags() != null ? new ArrayList<>(dto.tags()) : new ArrayList<>())
                .recruiter(recruiter)
                .build();

        return toDTO(jobRepository.save(job));
    }

    @Transactional
    public JobResponseDTO updateJob(Long id, JobRequestDTO dto, User recruiter) {
        Job job = findOwnedJob(id, recruiter);
        job.setTitle(dto.title());
        job.setDescription(dto.description());
        job.setLocation(dto.location());
        job.setModality(dto.modality());
        job.setLevel(dto.level());
        job.setSalary(dto.salary());
        if (dto.tags() != null) {
            job.getTags().clear();
            job.getTags().addAll(dto.tags());
        }
        return toDTO(jobRepository.save(job));
    }

    @Transactional(readOnly = true)
    public List<JobResponseDTO> getActiveJobs() {
        return jobRepository.findByStatusOrderByIdDesc(JobStatus.ATIVA)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<JobResponseDTO> getRecruiterJobs(User recruiter) {
        return jobRepository.findByRecruiterOrderByIdDesc(recruiter)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public void closeJob(Long id, User recruiter) {
        Job job = findOwnedJob(id, recruiter);
        job.setStatus(JobStatus.ENCERRADA);
        jobRepository.save(job);
    }

    @Transactional
    public void deleteJob(Long id, User recruiter) {
        Job job = findOwnedJob(id, recruiter);
        jobRepository.delete(job);
    }

    private Job findOwnedJob(Long id, User recruiter) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada."));
        if (!job.getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("Você não tem permissão para modificar esta vaga.");
        }
        return job;
    }

    private JobResponseDTO toDTO(Job job) {
        String company = job.getRecruiter().getCompany() != null
                ? job.getRecruiter().getCompany()
                : job.getRecruiter().getName();

        return JobResponseDTO.builder()
                .id(job.getId())
                .title(job.getTitle())
                .company(company)
                .companyLogo(logoGradient(company))
                .location(job.getLocation())
                .modality(job.getModality())
                .level(job.getLevel())
                .salary(formatSalary(job.getSalary()))
                .description(job.getDescription())
                .tags(job.getTags())
                .status(job.getStatus().name())
                .createdAt(job.getCreatedAt())
                .recruiterName(job.getRecruiter().getName())
                .build();
    }

    // ── Formatação de salário ───────────────────────────────────────────
    // "3000"         → "R$ 3.000"
    // "3000-5000"    → "R$ 3.000 – R$ 5.000"
    // "R$ 3.000"     → "R$ 3.000"  (já formatado, retorna sem alteração)
    private static String formatSalary(String salary) {
        if (salary == null || salary.isBlank()) return null;
        String s = salary.trim();
        if (s.toLowerCase().startsWith("r$")) return s;

        // Range: "3000-5000" ou "3000 – 5000"
        String[] parts = s.split("\\s*[\\-–]\\s*");
        if (parts.length == 2) {
            return "R$ " + formatBrNumber(parts[0]) + " – R$ " + formatBrNumber(parts[1]);
        }
        return "R$ " + formatBrNumber(s);
    }

    private static String formatBrNumber(String num) {
        String clean = num.trim().replaceAll("[^0-9]", "");
        if (clean.isEmpty()) return num.trim();
        try {
            long value = Long.parseLong(clean);
            // Formato brasileiro: pontos como separador de milhar
            String formatted = String.format("%,d", value); // usa vírgula por locale
            return formatted.replace(",", ".");
        } catch (NumberFormatException e) {
            return num.trim();
        }
    }

    private static String logoGradient(String company) {
        if (company == null || company.isBlank()) return LOGO_GRADIENTS[0];
        return LOGO_GRADIENTS[Math.abs(company.hashCode() % LOGO_GRADIENTS.length)];
    }
}
