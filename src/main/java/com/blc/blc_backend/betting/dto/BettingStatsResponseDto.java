package com.blc.blc_backend.betting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BettingStatsResponseDto {
    private Long gameId;
    private String homeTeamName;
    private String awayTeamName;

    private Long homeTeamBetPoints;
    private Integer homeTeamBetCount;
    private Double homeTeamOdds;

    private Long awayTeamBetPoints;
    private Integer awayTeamBetCount;
    private Double awayTeamOdds;

    private Long totalBetPoints;
    private Integer totalBetCount;

    private LocalDateTime lastUpdated;
}