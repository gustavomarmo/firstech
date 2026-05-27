package com.firstech.service;

import com.firstech.dto.AdminDashboardDTO;
import com.firstech.dto.AdminUserDTO;
import com.firstech.dto.AdminUserUpdateDTO;
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

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

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

        // ── Vagas por modalidade (ordem decrescente) ──────────────────────────
        Map<String, Long> vagasPorModalidade = jobs.stream()
                .filter(j -> j.getModality() != null && !j.getModality().isBlank())
                .collect(Collectors.groupingBy(Job::getModality, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey, Map.Entry::getValue,
                        (a, b) -> a, LinkedHashMap::new));

        // ── Vagas por nível (ordem lógica de carreira) ────────────────────────
        List<String> nivelOrder = List.of("Estágio", "Júnior", "Pleno", "Sênior");
        Map<String, Long> rawNivel = jobs.stream()
                .filter(j -> j.getLevel() != null && !j.getLevel().isBlank())
                .collect(Collectors.groupingBy(Job::getLevel, Collectors.counting()));
        Map<String, Long> vagasPorNivel = new LinkedHashMap<>();
        nivelOrder.forEach(n -> { if (rawNivel.containsKey(n)) vagasPorNivel.put(n, rawNivel.get(n)); });
        rawNivel.forEach((k, v) -> vagasPorNivel.putIfAbsent(k, v));

        // ── Top 5 postadores ──────────────────────────────────────────────────
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
                posts.size(),
                vagasPorModalidade, vagasPorNivel,
                topPosters
        );
    }

    // ── Gestão de usuários ────────────────────────────────────────────────────

    public List<AdminUserDTO> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<Post> posts = postRepository.findAll();

        Map<Long, Long> postCountByUser = posts.stream()
                .collect(Collectors.groupingBy(p -> p.getAuthor().getId(), Collectors.counting()));

        return users.stream()
                .map(u -> toDTO(u, postCountByUser.getOrDefault(u.getId(), 0L)))
                .sorted(Comparator.comparing(AdminUserDTO::criadoEm).reversed())
                .toList();
    }

    @Transactional
    public AdminUserDTO updateUser(Long id, AdminUserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuário não encontrado: " + id));

        if (dto.nome()  != null && !dto.nome().isBlank())  user.setName(dto.nome().trim());
        if (dto.email() != null && !dto.email().isBlank()) user.setEmail(dto.email().trim().toLowerCase());

        if (dto.role() != null && !dto.role().isBlank()) {
            Role novaRole = Role.valueOf(dto.role().toUpperCase());
            user.setRoles(new HashSet<>(Set.of(novaRole)));
        }

        userRepository.save(user);
        long postCount = postRepository.findByAuthorOrderByCreatedAtDesc(user).size();
        return toDTO(user, (long) postCount);
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

        // 1. Comentários feitos pelo usuário em posts de outros
        postCommentRepository.deleteByAuthor(user);

        // 2. Comentários de outros nos posts do usuário
        postCommentRepository.deleteByPostAuthor(user);

        // 3. Para recrutadores: nulificar linkedJob nos posts e deletar vagas
        if (user.getRoles().contains(Role.RECRUTADOR)) {
            postRepository.nullifyLinkedJobsByRecruiter(user);
            List<Job> recruiterJobs = jobRepository.findByRecruiterOrderByIdDesc(user);
            jobRepository.deleteAll(recruiterJobs); // JPA cascades job_tags
        }

        // 4. Posts do usuário (JPA cascades post_tags e post_likes)
        List<Post> userPosts = postRepository.findByAuthorOrderByCreatedAtDesc(user);
        postRepository.deleteAll(userPosts);

        // 5. Conexões
        connectionRepository.deleteByUser(user);

        // 6. Mensagens
        messageRepository.deleteByUser(user);

        // 7. Itens de portfólio
        experienceRepository.deleteByUser(user);
        skillRepository.deleteAllByUser(user);
        certificationRepository.deleteByUser(user);
        projectRepository.deleteByUser(user);

        // 8. Usuário (cascades RefreshToken e PasswordResetToken)
        userRepository.delete(user);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private AdminUserDTO toDTO(User u, long totalPosts) {
        String role = u.getRoles().isEmpty()
                ? "CANDIDATO"
                : u.getRoles().iterator().next().name();

        boolean isRec   = u.getRoles().contains(Role.RECRUTADOR);
        boolean isAdmin = u.getRoles().contains(Role.ADMINISTRADOR);

        String cargo;
        if (isAdmin)    cargo = "Administrador";
        else if (isRec) cargo = "Recrutador";
        else            cargo = labelCareerMoment(u.getCareerMoment());

        String empresa = isRec ? (u.getCompany() != null ? u.getCompany() : "") : "";
        String criadoEm = u.getCreatedAt() != null ? u.getCreatedAt().format(DATE_FMT) : "—";

        int conexoes = connectionRepository.countAcceptedConnections(u);

        return new AdminUserDTO(
                u.getId(),
                u.getName(),
                u.getEmail(),
                role,
                cargo,
                empresa,
                u.getCity() != null ? u.getCity() : "",
                u.isEnabled(),
                criadoEm,
                totalPosts,
                conexoes
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
