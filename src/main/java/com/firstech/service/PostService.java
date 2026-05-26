package com.firstech.service;

import com.firstech.dto.*;
import com.firstech.exception.ResourceNotFoundException;
import com.firstech.model.*;
import com.firstech.repository.*;
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

    private final PostRepository        postRepository;
    private final PostCommentRepository commentRepository;
    private final JobRepository         jobRepository;

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

        return toDTO(postRepository.save(post), author.getId());
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

        return toDTO(postRepository.save(post), requester.getId());
    }

    @Transactional
    public void deletePost(Long id, User requester) {
        Post post = findOwnedPost(id, requester);
        postRepository.delete(post);
    }

    /** Listagem sem usuário — likedByMe sempre false (chamada REST pública). */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getAllPosts() {
        return getAllPosts(null);
    }

    /** Listagem com usuário — popula likedByMe (chamada do ViewController). */
    @Transactional(readOnly = true)
    public List<PostResponseDTO> getAllPosts(User currentUser) {
        Long uid = currentUser != null ? currentUser.getId() : null;
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(p -> toDTO(p, uid)).toList();
    }

    // ── Curtidas ─────────────────────────────────────────────────────────

    @Transactional
    public LikeResponseDTO toggleLike(Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado."));
        Long uid = user.getId();
        boolean liked;
        if (post.getLikedByUserIds().contains(uid)) {
            post.getLikedByUserIds().remove(uid);
            liked = false;
        } else {
            post.getLikedByUserIds().add(uid);
            liked = true;
        }
        postRepository.save(post);
        return new LikeResponseDTO(liked, post.getLikedByUserIds().size());
    }

    // ── Comentários ───────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getComments(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId)
                .stream().map(this::toCommentDTO).toList();
    }

    @Transactional
    public CommentResponseDTO addComment(Long postId, String content, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Post não encontrado."));

        PostComment comment = PostComment.builder()
                .content(content)
                .post(post)
                .author(user)
                .build();

        post.setCommentCount(post.getCommentCount() + 1);
        postRepository.save(post);
        return toCommentDTO(commentRepository.save(comment));
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

    private PostResponseDTO toDTO(Post post, Long currentUserId) {
        User author = post.getAuthor();
        return PostResponseDTO.builder()
                .id(post.getId())
                .content(post.getContent())
                .title(post.getTitle())
                .tags(post.getTags())
                .autor(buildAutor(author))
                .likes(post.getLikedByUserIds().size())
                .likedByMe(currentUserId != null && post.getLikedByUserIds().contains(currentUserId))
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
                .avatarBase64(user.getAvatarBase64())
                .build();
    }

    private CommentResponseDTO toCommentDTO(PostComment c) {
        User a    = c.getAuthor();
        String nome = a.getName() != null ? a.getName() : "Usuário";
        return new CommentResponseDTO(
                c.getId(),
                c.getContent(),
                nome,
                nome.isBlank() ? "?" : nome.substring(0, 1).toUpperCase(),
                avatarGradient(nome),
                a.getAvatarBase64(),
                relativeTime(c.getCreatedAt())
        );
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

    /** Converte data de criação em string relativa. */
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
