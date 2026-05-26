package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "certifications")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Certification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Emoji representativo (ex: "☁️", "🏅"). */
    @Builder.Default
    private String emoji = "📜";

    @Column(nullable = false)
    private String name;

    /** Organização emissora. */
    private String issuer;

    /** Data de emissão em formato de exibição (ex: "Ago 2023"). */
    private String issueDate;

    @Builder.Default
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
