package com.firstech.service;

import com.firstech.dto.AdminDashboardDTO;
import com.firstech.model.*;
import com.firstech.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Lógica de negócio para o painel do administrador.
 */
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository       userRepository;
    private final JobRepository        jobRepository;
    private final PostRepository       postRepository;
    private final ConnectionRepository connectionRepository;

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

        // ── Vagas por modalidade (ordem decrescente de quantidade) ──────────
        Map<String, Long> vagasPorModalidade = jobs.stream()
                .filter(j -> j.getModality() != null && !j.getModality().isBlank())
                .collect(Collectors.groupingBy(Job::getModality, Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new));

        // ── Vagas por nível (ordem lógica de carreira) ──────────────────────
        List<String> nivelOrder = List.of("Estágio", "Júnior", "Pleno", "Sênior");
        Map<String, Long> rawNivel = jobs.stream()
                .filter(j -> j.getLevel() != null && !j.getLevel().isBlank())
                .collect(Collectors.groupingBy(Job::getLevel, Collectors.counting()));
        Map<String, Long> vagasPorNivel = new LinkedHashMap<>();
        nivelOrder.forEach(n -> { if (rawNivel.containsKey(n)) vagasPorNivel.put(n, rawNivel.get(n)); });
        rawNivel.forEach((k, v) -> vagasPorNivel.putIfAbsent(k, v)); // captura níveis extras

        // ── Top 5 postadores ────────────────────────────────────────────────
        Map<Long, User>  userById     = users.stream().collect(Collectors.toMap(User::getId, u -> u));
        Map<Long, Long>  postsByUser  = posts.stream()
                .collect(Collectors.groupingBy(p -> p.getAuthor().getId(), Collectors.counting()));
        List<AdminDashboardDTO.TopPosterEntry> topPosters = postsByUser.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(e -> {
                    User u  = userById.get(e.getKey());
                    boolean rec = u != null && u.getRoles().contains(Role.RECRUTADOR);
                    return new AdminDashboardDTO.TopPosterEntry(
                            u != null ? u.getName() : "Desconhecido", e.getValue(), rec);
                })
                .toList();

        // ── Últimos 5 usuários cadastrados (ID decrescente) ─────────────────
        List<AdminDashboardDTO.UserResumoEntry> ultimosUsuarios = users.stream()
                .sorted(Comparator.comparing(User::getId).reversed())
                .limit(5)
                .map(u -> new AdminDashboardDTO.UserResumoEntry(
                        u.getId(), u.getName(),
                        u.getRoles().contains(Role.RECRUTADOR),
                        u.getEmail()))
                .toList();

        // ── Últimas 5 vagas (ID decrescente) ────────────────────────────────
        List<AdminDashboardDTO.VagaResumoEntry> ultimasVagas = jobs.stream()
                .sorted(Comparator.comparing(Job::getId).reversed())
                .limit(5)
                .map(j -> new AdminDashboardDTO.VagaResumoEntry(
                        j.getId(),
                        j.getTitle(),
                        j.getJobCompany() != null ? j.getJobCompany() : "",
                        j.getLevel()      != null ? j.getLevel()      : "",
                        j.getModality()   != null ? j.getModality()   : "",
                        j.getStatus() == JobStatus.ATIVA))
                .toList();

        return new AdminDashboardDTO(
                users.size(), totalCandidatos, totalRecrutadores,
                jobs.size(), vagasAtivas, jobs.size() - vagasAtivas,
                posts.size(), connectionRepository.countAllAccepted(),
                vagasPorModalidade, vagasPorNivel,
                topPosters, ultimosUsuarios, ultimasVagas
        );
    }
}
