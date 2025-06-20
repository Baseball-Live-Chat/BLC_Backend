package com.blc.blc_backend.game.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Builder
@ToString
public class GameInfo {
    private final String homeCode;
    private final String awayCode;
    private final LocalDateTime gameDateTime;
    private final String stadium;
}

