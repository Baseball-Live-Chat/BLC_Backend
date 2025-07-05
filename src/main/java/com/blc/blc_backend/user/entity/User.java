package com.blc.blc_backend.user.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;

    private String profileImageUrl;

    private Long favoriteTeamId;

    @Column(nullable = false)
    private Long points = 0L;  // 포인트 (기본값 0)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole userRole = UserRole.USER;  // 권한 (기본값 USER)

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void addPoints(long points) {
        this.points += points;
    }

    public void subtractPoints(long points) {
        if (this.points < points) {
            throw new IllegalArgumentException("포인트가 부족합니다");
        }
        this.points -= points;
    }

    public boolean hasEnoughPoints(long requiredPoints) {
        return this.points >= requiredPoints;
    }
}
