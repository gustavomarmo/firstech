package com.firstech.service;

import com.firstech.dto.SearchResultDTO;
import com.firstech.model.*;
import com.firstech.repository.JobRepository;
import com.firstech.repository.PostRepository;
import com.firstech.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final UserRepository userRepository;
    private final JobRepository  jobRepository;
    private final PostRepository postRepository;

    private static final String[] AVATAR_GRADIENTS = {
            "linear-gradient(135deg,#6d28d9,#8b5cf6)",
            "linear-gradient(135deg,#1a56db,#3a86ff)",
            "linear-gradient(135deg,#0f6e56,#1d9e75)",
            "linear-gradient(135deg,#854f0b,#ef9f27)",
            "linear-gradient(135deg,#0e7490,#06b6d4)",
            "linear-gradient(135deg,#be185d,#f472b6)",
            "linear-gradient(135deg,#9a3412,#fb923c)",
    };

    /**
     * Busca unificada: candidatos + vagas ativas + posts.
     * Retorna lista mesclada na ordem: pessoas → vagas → posts.
     */
    @Transactional(readOnly = true)
    public List<SearchResultDTO> search(String q) {
        String term = q != null ? q.trim() : "";

        List<SearchResultDTO> results = new ArrayList<>();

        // ── Pessoas (candidatos) ──────────────────────────────────────────
        userRepository.searchByRole(Role.CANDIDATO, term).forEach(u -> {
            String nome    = u.getName() != null ? u.getName() : "Usuário";
            String inicial = nome.isBlank() ? "?" : initials(nome);
            String sub     = buildUserSubtitle(u);
            results.add(new SearchResultDTO(
                    u.getId(), nome, sub,
                    "pessoa", "Pessoa",
                    gradient(nome), inicial,
                    u.getAvatarBase64()
            ));
        });

        // ── Vagas ativas ─────────────────────────────────────────────────
        jobRepository.searchAtivas(term).forEach(j -> {
            String company = j.getJobCompany() != null ? j.getJobCompany() : "Empresa";
            String inicial = company.isBlank() ? "V" : company.substring(0, 1).toUpperCase();
            String sub     = buildJobSubtitle(j);
            results.add(new SearchResultDTO(
                    j.getId(),
                    j.getTitle() + (company.isBlank() ? "" : " — " + company),
                    sub,
                    "vaga", "Vaga",
                    gradient(company), inicial,
                    null
            ));
        });

        // ── Posts ─────────────────────────────────────────────────────────
        postRepository.searchByQuery(term).forEach(p -> {
            User author   = p.getAuthor();
            String nome   = author.getName() != null ? author.getName() : "Usuário";
            String titulo = p.getTitle() != null && !p.getTitle().isBlank()
                    ? p.getTitle()
                    : truncate(p.getContent(), 60);
            String sub    = "por " + nome + " · " + p.getLikes() + " curtidas";
            String inicial = nome.isBlank() ? "?" : nome.substring(0, 1).toUpperCase();
            results.add(new SearchResultDTO(
                    p.getId(), titulo, sub,
                    "post", "Post",
                    gradient(nome), inicial,
                    author.getAvatarBase64()
            ));
        });

        return results;
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private static String buildUserSubtitle(User u) {
        List<String> parts = new ArrayList<>();
        if (u.getTagline() != null && !u.getTagline().isBlank()) parts.add(u.getTagline());
        if (u.getCity()    != null && !u.getCity().isBlank())    parts.add(u.getCity());
        return parts.isEmpty() ? "Candidato" : String.join(" · ", parts);
    }

    private static String buildJobSubtitle(Job j) {
        List<String> parts = new ArrayList<>();
        if (j.getModality() != null && !j.getModality().isBlank()) parts.add(j.getModality());
        if (j.getLocation() != null && !j.getLocation().isBlank()) parts.add(j.getLocation());
        if (j.getSalary()   != null && !j.getSalary().isBlank())   parts.add(j.getSalary());
        return parts.isEmpty() ? "Vaga" : String.join(" · ", parts);
    }

    private static String initials(String name) {
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) return parts[0].substring(0, 1).toUpperCase();
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    private static String gradient(String name) {
        if (name == null || name.isBlank()) return AVATAR_GRADIENTS[0];
        return AVATAR_GRADIENTS[Math.abs(name.hashCode() % AVATAR_GRADIENTS.length)];
    }

    private static String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max) + "…";
    }
}
