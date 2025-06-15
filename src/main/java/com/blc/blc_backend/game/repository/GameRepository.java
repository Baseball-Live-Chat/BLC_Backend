package com.blc.blc_backend.game.repository;

import com.blc.blc_backend.game.entity.Game;
import com.blc.blc_backend.team.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    boolean existsByHomeTeamAndAwayTeamAndGameDate(Team home, Team away, LocalDateTime date);
}