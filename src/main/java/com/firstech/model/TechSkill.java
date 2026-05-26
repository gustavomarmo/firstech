package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tech_skills")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    /** Categoria para agrupamento (ex: "Linguagem", "Framework", "Banco de Dados"). */
    private String category;
}
