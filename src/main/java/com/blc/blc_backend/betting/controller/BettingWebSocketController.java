package com.blc.blc_backend.betting.controller;

import com.blc.blc_backend.betting.dto.BettingStatsResponseDto;
import com.blc.blc_backend.betting.service.BettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class BettingWebSocketController {

    private final BettingService bettingService;

    // WebSocket으로 베팅 현황 조회만 처리 (베팅은 REST에서 처리)
    @MessageMapping("/betting.getStats/{gameId}")
    @SendTo("/topic/betting/{gameId}")
    public BettingStatsResponseDto getBettingStats(@DestinationVariable Long gameId) {
        log.debug("WebSocket 베팅 현황 요청 - gameId: {}", gameId);
        return bettingService.calculateBettingStats(gameId);
    }
}