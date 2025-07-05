package com.blc.blc_backend.betting.service;

import com.blc.blc_backend.betting.dto.BettingStatsResponseDto;
import com.blc.blc_backend.betting.dto.UserBetStatusDto;

public interface BettingService {
    void placeBet(Long userId, Long gameId, Long predictedWinnerTeamId, int betPoints);
    BettingStatsResponseDto calculateBettingStats(Long gameId);
    UserBetStatusDto getUserBetStatus(Long userId, Long gameId);
    void settleBets(Long gameId, Long winnerTeamId);
}