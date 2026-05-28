package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "projects")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    /**
     * Gradiente CSS para o thumbnail (ex: "linear-gradient(135deg,#0f0521,#2d1b6e)").
     * Gerado a partir de um conjunto predefinido de temas.
     */
    @Builder.Default
    private String thumbGradient = "linear-gradient(135deg,#0f0521,#2d1b6e)";

    /**
     * Classe do ícone Tabler sem o prefixo "ti " (ex: "ti-code").
     * Mantido para compatibilidade com registros anteriores.
     */
    @Builder.Default
    private String icon = "ti-code";

    /** Data-URL da imagem de thumbnail enviada pelo usuário (data:image/...;base64,...). */
    @Column(columnDefinition = "TEXT")
    private String thumbImageBase64;

    /** URL do repositório no GitHub (para redirecionamento ao clicar no card). */
    @Column(length = 500)
    private String githubUrl;

    @Builder.Default
    private int orderIndex = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
