package com.firstech.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String password;

    @Builder.Default
    private boolean enabled = true;

    @Column(nullable = false)
    private LocalDate createdAt;

    // ── Perfil comum ──────────────────────────────────────────
    private String phone;
    private String linkedin;
    private String github;

    // ── Perfil talento ────────────────────────────────────────
    private String careerMoment;   // "estagio" | "junior" | "studying"
    private String workModel;      // "remote" | "hybrid" | "onsite"
    private String city;
    private String tagline;

    @Column(length = 2000)
    private String sobre;          // Seção "Sobre" do portfólio

    @Column(columnDefinition = "TEXT")
    private String avatarBase64;   // Data URL da foto de perfil (data:image/...;base64,...)

    @ElementCollection
    @CollectionTable(name = "user_technologies", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "technology")
    @Builder.Default
    private List<String> technologies = new ArrayList<>();

    // ── Perfil recrutador ─────────────────────────────────────
    private String company;
    private String companySize;
    private String website;

    @ElementCollection
    @CollectionTable(name = "user_culture_tags", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "culture_tag")
    @Builder.Default
    private List<String> cultureTags = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "user_goals", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "goal")
    @Builder.Default
    private List<String> goals = new ArrayList<>();

    // ── Papéis e relacionamentos ──────────────────────────────
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    @Builder.Default
    private Set<Role> roles = new HashSet<>(Set.of(Role.CANDIDATO));

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<RefreshToken> refreshTokens = new ArrayList<>();

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private PasswordResetToken passwordResetToken;

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired()     { return true; }

    @Override
    public boolean isAccountNonLocked()      { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
    }
}
