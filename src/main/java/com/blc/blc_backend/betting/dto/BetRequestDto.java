package com.blc.blc_backend.betting.dto;

import lombok.Data;

@Data
public class BetRequestDto {
    private Long gameId;
    private Long predictedWinnerTeamId;
    private Integer betPoints;
}