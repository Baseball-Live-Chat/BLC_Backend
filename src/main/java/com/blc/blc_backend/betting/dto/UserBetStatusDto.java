package com.blc.blc_backend.betting.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserBetStatusDto {
    private Long gameId;
    private Integer totalBetPoints;      // 현재 누적 베팅
    private Integer remainingPoints;     // 추가 가능 베팅
    private Long predictedWinnerTeamId;  // 예상 승리팀
    private Integer betCount;            // 베팅 횟수
    private Boolean canBet;              // 추가 베팅 가능 여부
}