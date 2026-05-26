package com.firstech.model;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "connections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"from_user_id","to_user_id"}))
@Builder @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Connection {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_user_id", nullable = false)
    private User fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_user_id", nullable = false)
    private User toUser;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private ConnectionStatus status = ConnectionStatus.PENDING;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() { createdAt = LocalDateTime.now(); }
}
