package com.blc.blc_backend.betting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_bets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameBet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bet_id")
    private Long betId;

    @Column(name = "game_id", nullable = false)
    private Long gameId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "predicted_winner_team_id", nullable = false)
    private Long predictedWinnerTeamId;

    @Column(name = "bet_points", nullable = false)
    private Integer betPoints;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "settled_at")
    private LocalDateTime settledAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return settledAt == null;
    }
}