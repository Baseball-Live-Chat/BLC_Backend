package com.blc.blc_backend.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class GameInfo {
    private final String homeCode;
    private final String awayCode;
    private final LocalDateTime gameDateTime;
    private final String stadium;
}

