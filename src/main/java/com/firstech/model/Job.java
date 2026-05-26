package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "jobs")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Job {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private String location;
    private String modality;   // "Remoto" | "Híbrido" | "Presencial"
    private String level;      // "Estágio" | "Júnior" | "Pleno"
    private String salary;

    /** Nome da empresa contratante para esta vaga (sobrescreve o padrão do recrutador). */
    private String jobCompany;

    /** URL da logo da empresa (se vazio, usa gradiente gerado automaticamente). */
    private String jobCompanyLogoUrl;

    @ElementCollection
    @CollectionTable(name = "job_tags", joinColumns = @JoinColumn(name = "job_id"))
    @Column(name = "tag")
    @Builder.Default
    private List<String> tags = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JobStatus status = JobStatus.ATIVA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recruiter_id", nullable = false)
    private User recruiter;

    @Column(nullable = false)
    private LocalDate createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }
}
