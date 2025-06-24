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

    @Column(unique = true, length = 128)
    private String firebaseUid;

    @Column(length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    private String profileImageUrl;

    private Long favoriteTeamId;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.modifiedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.modifiedAt = LocalDateTime.now();
    }
    
    // Convenience method for UserSyncService
    public Long getId() {
        return this.userId;
    }
}
