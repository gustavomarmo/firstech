package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "skills")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Se verdadeiro, exibido com destaque visual (badge roxo). */
    @Builder.Default
    private boolean highlight = false;

    @Builder.Default
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
