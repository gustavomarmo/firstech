package com.firstech.service;

import com.firstech.dto.PostAutorDTO;
import com.firstech.dto.PostRequestDTO;
import com.firstech.dto.PostResponseDTO;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.Job;
import com.firstech.model.Post;
import com.firstech.model.Role;
import com.firstech.model.User;
import com.firstech.repository.JobRepository;
import com.firstech.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final JobRepository  jobRepository;

    private static final String[] AVATAR_GRADIENTS = {
            "linear-gradient(135deg,#6d28d9,#8b5cf6)",
            "linear-gradient(135deg,#1a56db,#3a86ff)",
            "linear-gradient(135deg,#0f6e56,#1d9e75)",
            "linear-gradient(135deg,#854f0b,#ef9f27)",
            "linear-gradient(135deg,#0e7490,#06b6d4)",
            "linear-gradient(135deg,#be185d,#f472b6)",
            "linear-gradient(135deg,#9a3412,#fb923c)",
    };

    // ── CRUD ─────────────────────────────────────────────────────────────

    @Transactional
    public PostResponseDTO createPost(PostRequestDTO dto, User author) {
        Job linkedJob = resolveLinkedJob(dto.linkedJobId(), author);

        Post post = Post.builder()
                .content(dto.content())
                .title(dto.title())
                .tags(dto.tags() != null ? new ArrayList<>(dto.tags()) : new ArrayList<>())
                .author(author)
                .linkedJob(linkedJob)
                .build();

        return toDTO(postRepository.save(post));
    }

    @Transactional
    public PostResponseDTO updatePost(Long id, PostRequestDTO dto, User requester) {
        Post post = findOwnedPost(id, requester);

        post.setContent(dto.content());
        post.setTitle(dto.title());
        if (dto.tags() != null) {
            post.getTags().clear();
            post.getTags().addAll(dto.tags());
        }
        post.setLinkedJob(resolveLinkedJob(dto.linkedJobId(), requester));

        return toDTO(postRepository.save(post));
    }

    @Transactional
    public void deletePost(Long id, User requester) {
        Post post = findOwnedPost(id, requester);
        postRepository.delete(post);
    }

    @Transactional(readOnly = true)
    public List<PostResponseDTO> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(this::toDTO).toList();
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private Post findOwnedPost(Long id, User requester) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado."));
        if (!post.getAuthor().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Você não tem permissão para modificar este post.");
        }
        return post;
    }

    private Job resolveLinkedJob(Long jobId, User requester) {
        if (jobId == null) return null;
        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new ResourceNotFoundException("Vaga não encontrada."));
        if (!job.getRecruiter().getId().equals(requester.getId())) {
            throw new AccessDeniedException("Você não pode vincular vagas de outros recrutadores.");
        }
        return job;
    }

    private PostResponseDTO toDTO(Post post) {
        User author = post.getAuthor();
        return PostResponseDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .title(post.getTitle())
                .tags(post.getTags())
                .autor(buildAutor(author))
                .likes(post.getLikes())
                .commentCount(post.getCommentCount())
                .tempoRelativo(relativeTime(post.getCreatedAt()))
                .linkedJobId(post.getLinkedJob() != null ? post.getLinkedJob().getId() : null)
                .linkedJobTitle(post.getLinkedJob() != null ? post.getLinkedJob().getTitle() : null)
                .createdAt(post.getCreatedAt())
                .build();
    }

    private PostAutorDTO buildAutor(User user) {
        String nome    = user.getName() != null ? user.getName() : "Usuário";
        String inicial = nome.isBlank() ? "?" : nome.substring(0, 1).toUpperCase();
        return PostAutorDTO.builder()
                .id(user.getId())
                .nome(nome)
                .cargo(cargoLabel(user))
                .corAvatar(avatarGradient(nome))
                .inicial(inicial)
                .build();
    }

    /** Gera label de cargo para exibição no card. */
    private static String cargoLabel(User user) {
        boolean isRecruiter = user.getRoles().contains(Role.RECRUTADOR);
        if (isRecruiter) {
            return user.getCompany() != null && !user.getCompany().isBlank()
                    ? "Recrutador · " + user.getCompany()
                    : "Recrutador";
        }
        return switch (user.getCareerMoment() != null ? user.getCareerMoment() : "") {
            case "estagio"  -> "Estagiário";
            case "junior"   -> "Dev Júnior";
            case "studying" -> "Estudante";
            default         -> "Desenvolvedor";
        };
    }

    /** Gradiente determinístico baseado no nome do usuário. */
    private static String avatarGradient(String name) {
        if (name == null || name.isBlank()) return AVATAR_GRADIENTS[0];
        return AVATAR_GRADIENTS[Math.abs(name.hashCode() % AVATAR_GRADIENTS.length)];
    }

    /** Converte data de criação em string relativa: "agora", "5m atrás", "2h atrás", "3d atrás". */
    private static String relativeTime(LocalDateTime createdAt) {
        if (createdAt == null) return "";
        long minutes = ChronoUnit.MINUTES.between(createdAt, LocalDateTime.now());
        if (minutes < 1)   return "agora";
        if (minutes < 60)  return minutes + "m atrás";
        long hours = minutes / 60;
        if (hours < 24)    return hours + "h atrás";
        long days = hours / 24;
        if (days < 7)      return days + "d atrás";
        long weeks = days / 7;
        return weeks + " sem. atrás";
    }
}
