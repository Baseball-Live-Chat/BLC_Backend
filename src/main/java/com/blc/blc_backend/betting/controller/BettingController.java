package com.blc.blc_backend.betting.controller;

import com.blc.blc_backend.betting.dto.BetRequestDto;
import com.blc.blc_backend.betting.dto.BetResponseDto;
import com.blc.blc_backend.betting.dto.BettingStatsResponseDto;
import com.blc.blc_backend.betting.dto.UserBetStatusDto;
import com.blc.blc_backend.betting.service.BettingService;
import com.blc.blc_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/betting")
@RequiredArgsConstructor
public class BettingController {

    private final BettingService bettingService;
    private final UserService userService;

    // 베팅하기 (HTTP POST)
    @PostMapping
    public ResponseEntity<BetResponseDto> placeBet(
            @RequestBody BetRequestDto request,
            Authentication authentication) {

        String username = authentication.getName();
        Long userId = userService.getUserIdByUsername(username);

        // 베팅 처리 (Service에서 자동으로 WebSocket 브로드캐스트됨)
        bettingService.placeBet(
                userId,
                request.getGameId(),
                request.getPredictedWinnerTeamId(),
                request.getBetPoints()
        );

        BetResponseDto response = BetResponseDto.builder()
                .gameId(request.getGameId())
                .userId(userId)
                .predictedWinnerTeamId(request.getPredictedWinnerTeamId())
                .betPoints(request.getBetPoints())
                .message("베팅이 완료되었습니다")
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 베팅 현황 조회 (HTTP GET)
    @GetMapping("/games/{gameId}/stats")
    public ResponseEntity<BettingStatsResponseDto> getBettingStats(@PathVariable Long gameId) {
        BettingStatsResponseDto stats = bettingService.calculateBettingStats(gameId);
        return ResponseEntity.ok(stats);
    }

    // 사용자 베팅 현황 조회 (HTTP GET)
    @GetMapping("/games/{gameId}/my-status")
    public ResponseEntity<UserBetStatusDto> getMyBetStatus(
            @PathVariable Long gameId,
            Authentication authentication) {

        String username = authentication.getName();
        Long userId = userService.getUserIdByUsername(username);

        UserBetStatusDto status = bettingService.getUserBetStatus(userId, gameId);
        return ResponseEntity.ok(status);
    }

    // 관리자용 베팅 정산 (HTTP POST)
    @PostMapping("/games/{gameId}/settle")
    public ResponseEntity<String> settleBets(
            @PathVariable Long gameId,
            @RequestParam(required = false) Long winnerTeamId) {

        bettingService.settleBets(gameId, winnerTeamId);
        return ResponseEntity.ok("베팅 정산이 완료되었습니다");
    }
}