package com.firstech.service;

import com.firstech.dto.*;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.*;
import com.firstech.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final UserRepository          userRepository;
    private final ExperienceRepository    experienceRepository;
    private final SkillRepository         skillRepository;
    private final CertificationRepository certificationRepository;
    private final ProjectRepository       projectRepository;
    private final TechSkillRepository     techSkillRepository;

    // Gradientes para logos de empresas (determinístico via hash)
    private static final String[] COMPANY_GRADIENTS = {
            "linear-gradient(135deg,#6d28d9,#8b5cf6)",
            "linear-gradient(135deg,#1a56db,#3a86ff)",
            "linear-gradient(135deg,#0f6e56,#1d9e75)",
            "linear-gradient(135deg,#854f0b,#ef9f27)",
            "linear-gradient(135deg,#0e7490,#06b6d4)",
            "linear-gradient(135deg,#be185d,#f472b6)",
            "linear-gradient(135deg,#9a3412,#fb923c)",
    };

    // ── Perfil ────────────────────────────────────────────────────────────

    @Transactional
    public void updateProfile(ProfileUpdateDTO dto, User user) {
        if (dto.name() != null && !dto.name().isBlank()) user.setName(dto.name());
        if (dto.headline() != null) user.setTagline(dto.headline());
        if (dto.city() != null)     user.setCity(dto.city());
        if (dto.about()  != null)   user.setSobre(dto.about());
        userRepository.save(user);
    }

    @Transactional
    public void updateAvatar(MultipartFile file, User user) throws IOException {
        String ct = file.getContentType();
        if (ct == null || !ct.startsWith("image/"))
            throw new IllegalArgumentException("O arquivo deve ser uma imagem (JPEG, PNG, WebP, etc.).");
        if (file.getSize() > 2 * 1024 * 1024)
            throw new IllegalArgumentException("A imagem deve ter no máximo 2 MB.");
        String b64 = Base64.getEncoder().encodeToString(file.getBytes());
        user.setAvatarBase64("data:" + ct + ";base64," + b64);
        userRepository.save(user);
    }

    // ── Leitura completa para o ViewController ───────────────────────────

    @Transactional(readOnly = true)
    public List<ExperienciaViewModel> getExperiences(User user) {
        return experienceRepository.findByUserOrderByOrderIndexAsc(user)
                .stream().map(this::toExpVM).toList();
    }

    @Transactional(readOnly = true)
    public List<HabilidadeViewModel> getSkills(User user) {
        return skillRepository.findByUserOrderByOrderIndexAsc(user)
                .stream().map(this::toSkillVM).toList();
    }

    @Transactional(readOnly = true)
    public List<CertificacaoViewModel> getCertifications(User user) {
        return certificationRepository.findByUserOrderByOrderIndexAsc(user)
                .stream().map(this::toCertVM).toList();
    }

    @Transactional(readOnly = true)
    public List<ProjetoViewModel> getProjects(User user) {
        return projectRepository.findByUserOrderByOrderIndexAsc(user)
                .stream().map(this::toProjVM).toList();
    }

    // ── Experiências ─────────────────────────────────────────────────────

    @Transactional
    public ExperienciaViewModel addExperience(ExperienceRequestDTO dto, User user) {
        Experience exp = Experience.builder()
                .title(dto.title())
                .companyName(dto.companyName())
                .period(dto.period())
                .description(dto.description())
                .orderIndex(dto.orderIndex())
                .user(user)
                .build();
        return toExpVM(experienceRepository.save(exp));
    }

    @Transactional
    public ExperienciaViewModel updateExperience(Long id, ExperienceRequestDTO dto, User user) {
        Experience exp = ownedExperience(id, user);
        exp.setTitle(dto.title());
        exp.setCompanyName(dto.companyName());
        exp.setPeriod(dto.period());
        exp.setDescription(dto.description());
        exp.setOrderIndex(dto.orderIndex());
        return toExpVM(experienceRepository.save(exp));
    }

    @Transactional
    public void deleteExperience(Long id, User user) {
        experienceRepository.delete(ownedExperience(id, user));
    }

    // ── Habilidades ──────────────────────────────────────────────────────

    @Transactional
    public HabilidadeViewModel addSkill(SkillRequestDTO dto, User user) {
        Skill skill = Skill.builder()
                .name(dto.name())
                .highlight(dto.highlight())
                .orderIndex(dto.orderIndex())
                .user(user)
                .build();
        return toSkillVM(skillRepository.save(skill));
    }

    @Transactional
    public HabilidadeViewModel updateSkill(Long id, SkillRequestDTO dto, User user) {
        Skill skill = ownedSkill(id, user);
        skill.setName(dto.name());
        skill.setHighlight(dto.highlight());
        skill.setOrderIndex(dto.orderIndex());
        return toSkillVM(skillRepository.save(skill));
    }

    @Transactional
    public void deleteSkill(Long id, User user) {
        skillRepository.delete(ownedSkill(id, user));
    }

    // ── Certificações ─────────────────────────────────────────────────────

    @Transactional
    public CertificacaoViewModel addCertification(CertificationRequestDTO dto, User user) {
        Certification cert = Certification.builder()
                .emoji(dto.emoji() != null && !dto.emoji().isBlank() ? dto.emoji() : "📜")
                .name(dto.name())
                .issuer(dto.issuer())
                .issueDate(dto.issueDate())
                .orderIndex(dto.orderIndex())
                .user(user)
                .build();
        return toCertVM(certificationRepository.save(cert));
    }

    @Transactional
    public CertificacaoViewModel updateCertification(Long id, CertificationRequestDTO dto, User user) {
        Certification cert = ownedCertification(id, user);
        cert.setEmoji(dto.emoji() != null && !dto.emoji().isBlank() ? dto.emoji() : cert.getEmoji());
        cert.setName(dto.name());
        cert.setIssuer(dto.issuer());
        cert.setIssueDate(dto.issueDate());
        cert.setOrderIndex(dto.orderIndex());
        return toCertVM(certificationRepository.save(cert));
    }

    @Transactional
    public void deleteCertification(Long id, User user) {
        certificationRepository.delete(ownedCertification(id, user));
    }

    // ── Projetos ─────────────────────────────────────────────────────────

    @Transactional
    public ProjetoViewModel addProject(ProjectRequestDTO dto, User user) {
        Project proj = Project.builder()
                .name(dto.name())
                .description(dto.description())
                .thumbGradient(dto.thumbGradient() != null ? dto.thumbGradient() : "linear-gradient(135deg,#0f0521,#2d1b6e)")
                .icon(dto.icon() != null ? dto.icon() : "ti-code")
                .thumbImageBase64(dto.thumbImageBase64())
                .githubUrl(dto.githubUrl())
                .orderIndex(dto.orderIndex())
                .user(user)
                .build();
        return toProjVM(projectRepository.save(proj));
    }

    @Transactional
    public ProjetoViewModel updateProject(Long id, ProjectRequestDTO dto, User user) {
        Project proj = ownedProject(id, user);
        proj.setName(dto.name());
        proj.setDescription(dto.description());
        if (dto.thumbGradient() != null)    proj.setThumbGradient(dto.thumbGradient());
        if (dto.icon() != null)             proj.setIcon(dto.icon());
        // null significa "manter imagem atual"; string vazia significa "remover imagem"
        if (dto.thumbImageBase64() != null) proj.setThumbImageBase64(
                dto.thumbImageBase64().isBlank() ? null : dto.thumbImageBase64());
        proj.setGithubUrl(dto.githubUrl());
        proj.setOrderIndex(dto.orderIndex());
        return toProjVM(projectRepository.save(proj));
    }

    /** Retorna os nomes de todas as habilidades do catálogo, em ordem alfabética. */
    @Transactional(readOnly = true)
    public List<String> getSkillsCatalog() {
        return techSkillRepository.findAllByOrderByNameAsc()
                .stream().map(TechSkill::getName).toList();
    }

    @Transactional
    public void deleteProject(Long id, User user) {
        projectRepository.delete(ownedProject(id, user));
    }

    // ── Mapeamentos ──────────────────────────────────────────────────────

    private ExperienciaViewModel toExpVM(Experience exp) {
        String cn = exp.getCompanyName() != null ? exp.getCompanyName() : "";
        return ExperienciaViewModel.builder()
                .id(exp.getId())
                .cargo(exp.getTitle())
                .empresa(EmpresaViewModel.builder()
                        .nome(cn)
                        .corLogo(companyGradient(cn))
                        .inicial(cn.isBlank() ? "E" : cn.substring(0, 1).toUpperCase())
                        .build())
                .periodo(exp.getPeriod())
                .descricao(exp.getDescription())
                .orderIndex(exp.getOrderIndex())
                .build();
    }

    private HabilidadeViewModel toSkillVM(Skill skill) {
        return HabilidadeViewModel.builder()
                .id(skill.getId())
                .nome(skill.getName())
                .destaque(skill.isHighlight())
                .orderIndex(skill.getOrderIndex())
                .build();
    }

    private CertificacaoViewModel toCertVM(Certification cert) {
        return CertificacaoViewModel.builder()
                .id(cert.getId())
                .emoji(cert.getEmoji())
                .nome(cert.getName())
                .emissor(cert.getIssuer())
                .dataEmissao(cert.getIssueDate())
                .orderIndex(cert.getOrderIndex())
                .build();
    }

    private ProjetoViewModel toProjVM(Project proj) {
        return ProjetoViewModel.builder()
                .id(proj.getId())
                .nome(proj.getName())
                .descricao(proj.getDescription())
                .corThumb(proj.getThumbGradient())
                .icone(proj.getIcon())
                .thumbImageBase64(proj.getThumbImageBase64())
                .githubUrl(proj.getGithubUrl())
                .orderIndex(proj.getOrderIndex())
                .build();
    }

    // ── Helpers de verificação de propriedade ─────────────────────────────

    private Experience ownedExperience(Long id, User user) {
        Experience e = experienceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Experiência não encontrada."));
        if (!e.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("Sem permissão.");
        return e;
    }

    private Skill ownedSkill(Long id, User user) {
        Skill s = skillRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Habilidade não encontrada."));
        if (!s.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("Sem permissão.");
        return s;
    }

    private Certification ownedCertification(Long id, User user) {
        Certification c = certificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Certificação não encontrada."));
        if (!c.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("Sem permissão.");
        return c;
    }

    private Project ownedProject(Long id, User user) {
        Project p = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Projeto não encontrado."));
        if (!p.getUser().getId().equals(user.getId()))
            throw new AccessDeniedException("Sem permissão.");
        return p;
    }

    private static String companyGradient(String name) {
        if (name == null || name.isBlank()) return COMPANY_GRADIENTS[0];
        return COMPANY_GRADIENTS[Math.abs(name.hashCode() % COMPANY_GRADIENTS.length)];
    }
}
