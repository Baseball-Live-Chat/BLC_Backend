package com.blc.blc_backend.betting.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BetResponseDto {
    private Long betId;
    private Long gameId;
    private Long userId;
    private Long predictedWinnerTeamId;
    private Integer betPoints;
    private LocalDateTime createdAt;
    private String message;
}