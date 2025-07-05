package com.blc.blc_backend.betting.repository;

import com.blc.blc_backend.betting.dto.BettingStatsProjection;
import com.blc.blc_backend.betting.entity.GameBet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameBetRepository extends JpaRepository<GameBet, Long> {

    // 정산되지 않은 베팅 조회
    List<GameBet> findByGameIdAndSettledAtIsNull(Long gameId);

    // 사용자별 베팅 내역
    List<GameBet> findByUserIdOrderByCreatedAtDesc(Long userId);

    // 특정 유저의 특정 경기 베팅들
    List<GameBet> findByUserIdAndGameIdAndSettledAtIsNull(Long userId, Long gameId);

    // 특정 유저의 특정 경기 누적 베팅 금액
    @Query("SELECT COALESCE(SUM(gb.betPoints), 0) FROM GameBet gb " +
            "WHERE gb.userId = :userId AND gb.gameId = :gameId AND gb.settledAt IS NULL")
    Integer getTotalBetPointsByUserAndGame(@Param("userId") Long userId, @Param("gameId") Long gameId);

    // 실시간 베팅 통계 계산
    @Query(value = """
        SELECT 
            SUM(CASE WHEN predicted_winner_team_id = :homeTeamId THEN bet_points ELSE 0 END) as homePoints,
            SUM(CASE WHEN predicted_winner_team_id = :awayTeamId THEN bet_points ELSE 0 END) as awayPoints,
            COUNT(DISTINCT CASE WHEN predicted_winner_team_id = :homeTeamId THEN user_id END) as homeCount,
            COUNT(DISTINCT CASE WHEN predicted_winner_team_id = :awayTeamId THEN user_id END) as awayCount,
            SUM(bet_points) as totalPoints,
            COUNT(DISTINCT user_id) as totalCount
        FROM game_bets 
        WHERE game_id = :gameId AND settled_at IS NULL
        """, nativeQuery = true)
    BettingStatsProjection getBettingStatsByGameId(
            @Param("gameId") Long gameId,
            @Param("homeTeamId") Long homeTeamId,
            @Param("awayTeamId") Long awayTeamId
    );
}