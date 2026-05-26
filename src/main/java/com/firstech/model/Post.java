package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Conteúdo principal (obrigatório). */
    @Column(nullable = false, length = 2000)
    private String content;

    /** Título opcional — candidatos podem usar para nomear projetos. */
    private String title;

    @ElementCollection
    @CollectionTable(name = "post_tags", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    /** Vaga vinculada (opcional — usada por recrutadores). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linked_job_id")
    private Job linkedJob;

    /** IDs dos usuários que curtiram este post (tabela de junção simples). */
    @ElementCollection
    @CollectionTable(name = "post_likes", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "user_id")
    @Builder.Default
    private java.util.Set<Long> likedByUserIds = new java.util.HashSet<>();

    @Builder.Default
    private int commentCount = 0;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
