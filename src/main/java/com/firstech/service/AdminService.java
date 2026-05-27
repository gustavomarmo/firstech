package com.firstech.service;

import com.firstech.dto.*;
import com.firstech.model.*;
import com.firstech.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Lógica de negócio para o painel do administrador.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository            userRepository;
    private final JobRepository             jobRepository;
    private final PostRepository            postRepository;
    private final PostCommentRepository     postCommentRepository;
    private final ConnectionRepository      connectionRepository;
    private final MessageRepository         messageRepository;
    private final ExperienceRepository      experienceRepository;
    private final SkillRepository           skillRepository;
    private final CertificationRepository   certificationRepository;
    private final ProjectRepository         projectRepository;

    private static final DateTimeFormatter DATE_FMT     = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ── Dashboard ─────────────────────────────────────────────────────────────

    public AdminDashboardDTO getDashboard() {
        List<User> users = userRepository.findAll();
        List<Job>  jobs  = jobRepository.findAll();
        List<Post> posts = postRepository.findAll();

        long totalCandidatos   = users.stream()
                .filter(u -> u.getRoles().contains(Role.CANDIDATO)).count();
        long totalRecrutadores = users.stream()
                .filter(u -> u.getRoles().contains(Role.RECRUTADOR)).count();
        long vagasAtivas       = jobs.stream()
                .filter(j -> j.getStatus() == JobStatus.ATIVA).count();

        Map<String, Long> vagasPorModalidade = jobs.stream()
                .filter(j -> j.getModality() != null && !j.getModality().isBlank())
                .collect(Collectors.groupingBy(Job::getModality, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        List<String> nivelOrder = List.of("Estágio", "Júnior", "Pleno", "Sênior");
        Map<String, Long> rawNivel = jobs.stream()
                .filter(j -> j.getLevel() != null && !j.getLevel().isBlank())
                .collect(Collectors.groupingBy(Job::getLevel, Collectors.counting()));
        Map<String, Long> vagasPorNivel = new LinkedHashMap<>();
        nivelOrder.forEach(n -> { if (rawNivel.containsKey(n)) vagasPorNivel.put(n, rawNivel.get(n)); });
        rawNivel.forEach((k, v) -> vagasPorNivel.putIfAbsent(k, v));

        Map<Long, User> userById    = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Long> postsByUser = posts.stream()
                .collect(Collectors.groupingBy(p -> p.getAuthor().getId(), Collectors.counting()));
        List<AdminDashboardDTO.TopPosterEntry> topPosters = postsByUser.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    User u   = userById.get(e.getKey());
                    boolean rec = u != null && u.getRoles().contains(Role.RECRUTADOR);
                    return new AdminDashboardDTO.TopPosterEntry(
                            u != null ? u.getName() : "Desconhecido", e.getValue(), rec);
                })
                .toList();

        return new AdminDashboardDTO(
                users.size(), totalCandidatos, totalRecrutadores,
                jobs.size(), vagasAtivas, jobs.size() - vagasAtivas,
                posts.size(), vagasPorModalidade, vagasPorNivel, topPosters
        );
    }

    // ── Gestão de usuários ────────────────────────────────────────────────────

    public List<AdminUserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Post> posts = postRepository.findAll();
        Map<Long, Long> postCountByUser = posts.stream()
                .collect(Collectors.groupingBy(p -> p.getAuthor().getId(), Collectors.counting()));
        return users.stream()
                .map(u -> toUserDTO(u, postCountByUser.getOrDefault(u.getId(), 0L)))
                .sorted(Comparator.comparing(AdminUserDTO::criadoEm).reversed())
                .toList();
    }

    @Transactional
    public AdminUserDTO updateUser(Long id, AdminUserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + id));
        if (dto.nome()  != null && !dto.nome().isBlank())  user.setName(dto.nome().trim());
        if (dto.email() != null && !dto.email().isBlank()) user.setEmail(dto.email().trim().toLowerCase());
        if (dto.role()  != null && !dto.role().isBlank()) {
            user.setRoles(new HashSet<>(Set.of(Role.valueOf(dto.role().toUpperCase()))));
        }
        userRepository.save(user);
        long postCount = postRepository.findByAuthorOrderByCreatedAtDesc(user).size();
        return toUserDTO(user, (long) postCount);
    }

    @Transactional
    public void toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + id));
        user.setEnabled(!user.isEnabled());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + id));
        postCommentRepository.deleteByAuthor(user);
        postCommentRepository.deleteByPostAuthor(user);
        if (user.getRoles().contains(Role.RECRUTADOR)) {
            postRepository.nullifyLinkedJobsByRecruiter(user);
            jobRepository.deleteAll(jobRepository.findByRecruiterOrderByIdDesc(user));
        }
        postRepository.deleteAll(postRepository.findByAuthorOrderByCreatedAtDesc(user));
        connectionRepository.deleteByUser(user);
        messageRepository.deleteByUser(user);
        experienceRepository.deleteByUser(user);
        skillRepository.deleteAllByUser(user);
        certificationRepository.deleteByUser(user);
        projectRepository.deleteByUser(user);
        userRepository.delete(user);
    }

    // ── Gestão de posts ───────────────────────────────────────────────────────

    public List<AdminPostDTO> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toPostDTO)
                .toList();
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Post não encontrado: " + id));
        postCommentRepository.deleteByPostId(id);
        postRepository.delete(post); // JPA cascades post_tags e post_likes
    }

    // ── Gestão de vagas ───────────────────────────────────────────────────────

    public List<AdminJobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
                .sorted(Comparator.comparing(Job::getCreatedAt).reversed())
                .map(this::toJobDTO)
                .toList();
    }

    @Transactional
    public AdminJobDTO toggleJobStatus(Long id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vaga não encontrada: " + id));
        job.setStatus(job.getStatus() == JobStatus.ATIVA ? JobStatus.ENCERRADA : JobStatus.ATIVA);
        jobRepository.save(job);
        return toJobDTO(job);
    }

    @Transactional
    public void deleteJob(Long id) {
        jobRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Vaga não encontrada: " + id));
        postRepository.nullifyLinkedJobById(id); // desvincula posts antes de deletar
        jobRepository.deleteById(id);            // JPA cascades job_tags
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AdminUserDTO toUserDTO(User u, long totalPosts) {
        String role = u.getRoles().isEmpty() ? "CANDIDATO" : u.getRoles().iterator().next().name();
        boolean isRec   = u.getRoles().contains(Role.RECRUTADOR);
        boolean isAdmin = u.getRoles().contains(Role.ADMINISTRADOR);
        String cargo = isAdmin ? "Administrador" : isRec ? "Recrutador" : labelCareerMoment(u.getCareerMoment());
        String empresa  = isRec ? (u.getCompany() != null ? u.getCompany() : "") : "";
        String criadoEm = u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FMT) : "—";
        int conexoes    = connectionRepository.countAcceptedConnections(u);
        return new AdminUserDTO(u.getId(), u.getName(), u.getEmail(), role, cargo, empresa,
                u.getCity() != null ? u.getCity() : "", u.isEnabled(), criadoEm, totalPosts, conexoes);
    }

    private AdminPostDTO toPostDTO(Post p) {
        User author    = p.getAuthor();
        String role    = author.getRoles().isEmpty() ? "CANDIDATO" : author.getRoles().iterator().next().name();
        String content = p.getContent();
        String snippet = content != null && content.length() > 140
                ? content.substring(0, 140) + "…" : content;
        String criadoEm = p.getCreatedAt() != null ? p.getCreatedAt().format(DATETIME_FMT) : "—";
        return new AdminPostDTO(
                p.getId(),
                author.getId(), author.getName(), author.getEmail(), role,
                p.getTitle(),
                snippet,
                p.getTags() != null ? p.getTags() : List.of(),
                p.getLikedByUserIds() != null ? p.getLikedByUserIds().size() : 0,
                p.getCommentCount(),
                criadoEm
        );
    }

    private AdminJobDTO toJobDTO(Job j) {
        User rec      = j.getRecruiter();
        String empresa = j.getJobCompany() != null && !j.getJobCompany().isBlank()
                ? j.getJobCompany()
                : (rec != null && rec.getCompany() != null ? rec.getCompany() : "—");
        String criadoEm = j.getCreatedAt() != null ? j.getCreatedAt().format(DATE_FMT) : "—";
        return new AdminJobDTO(
                j.getId(),
                j.getTitle(),
                empresa,
                rec != null ? rec.getId()    : null,
                rec != null ? rec.getName()  : "—",
                rec != null ? rec.getEmail() : "—",
                j.getModality()  != null ? j.getModality()  : "—",
                j.getLevel()     != null ? j.getLevel()     : "—",
                j.getSalary()    != null ? j.getSalary()    : "—",
                j.getLocation()  != null ? j.getLocation()  : "—",
                j.getStatus()    != null ? j.getStatus().name() : "ATIVA",
                j.getTags()      != null ? j.getTags()      : List.of(),
                criadoEm
        );
    }

    private static String labelCareerMoment(String cm) {
        if (cm == null) return "Desenvolvedor";
        return switch (cm) {
            case "estagio"  -> "Estagiário";
            case "junior"   -> "Dev Júnior";
            case "studying" -> "Estudante";
            default         -> "Desenvolvedor";
        };
    }
}
