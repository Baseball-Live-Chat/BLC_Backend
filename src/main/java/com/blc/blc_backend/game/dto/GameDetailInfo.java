package com.blc.blc_backend.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameDetailInfo {
    private final Long gameId;
    private final String homeTeamName;
    private final String homeCode;
    private final String homeLogoUrl;
    private final String homeTeamColor;
    private final String awayTeamName;
    private final String awayCode;
    private final String awayLogoUrl;
    private final String awayTeamColor;
    private final LocalDateTime gameDateTime;
    private final String stadium;
}