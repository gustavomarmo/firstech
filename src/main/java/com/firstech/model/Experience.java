package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "experiences")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Experience {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Cargo / título do cargo. */
    @Column(nullable = false)
    private String title;

    /** Nome da empresa. */
    @Column(nullable = false)
    private String companyName;

    /**
     * Período de exibição (ex: "Jan 2024 – Presente · 1 ano · São Paulo, SP").
     * Armazenado como string formatada para máxima flexibilidade.
     */
    private String period;

    @Column(length = 1000)
    private String description;

    /** Ordem de exibição (0 = mais recente). */
    @Builder.Default
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
