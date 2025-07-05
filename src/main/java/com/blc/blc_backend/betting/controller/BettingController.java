package com.blc.blc_backend.betting.controller;

import com.blc.blc_backend.betting.dto.BetRequestDto;
import com.blc.blc_backend.betting.dto.BetResponseDto;
import com.blc.blc_backend.betting.dto.BettingStatsResponseDto;
import com.blc.blc_backend.betting.dto.UserBetStatusDto;
import com.blc.blc_backend.betting.service.BettingService;
import com.blc.blc_backend.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Betting API", description = "게임 베팅 관련 CRUD 및 통계 조회 API")
@RestController
@RequestMapping("/api/betting")
@RequiredArgsConstructor
public class BettingController {

    private final BettingService bettingService;
    private final UserService userService;

    @Operation(
            summary = "베팅 등록",
            description = "인증된 사용자가 특정 게임에 베팅을 등록합니다. 성공 시 201 Created와 함께 BetResponseDto를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "베팅이 성공적으로 등록됨",
                    content = @Content(schema = @Schema(implementation = BetResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 (예: 포인트 부족 등)"),
            @ApiResponse(responseCode = "401", description = "인증 실패")
    })
    @PostMapping
    public ResponseEntity<BetResponseDto> placeBet(
            @Parameter(description = "베팅 요청 정보", required = true,
                    schema = @Schema(implementation = BetRequestDto.class))
            @RequestBody BetRequestDto request,
            Authentication authentication) {

        String username = authentication.getName();
        Long userId = userService.getUserIdByUsername(username);

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

    @Operation(
            summary = "게임별 베팅 통계 조회",
            description = "특정 게임에 대한 전체 유저의 베팅 현황 통계를 반환합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "통계 조회 성공",
                    content = @Content(schema = @Schema(implementation = BettingStatsResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "해당 게임을 찾을 수 없음")
    })
    @GetMapping("/games/{gameId}/stats")
    public ResponseEntity<BettingStatsResponseDto> getBettingStats(
            @Parameter(description = "통계를 조회할 게임 ID", example = "123", required = true)
            @PathVariable Long gameId) {

        BettingStatsResponseDto stats = bettingService.calculateBettingStats(gameId);
        return ResponseEntity.ok(stats);
    }

    @Operation(
            summary = "내 베팅 현황 조회",
            description = "인증된 사용자의 특정 게임에 대한 베팅 상태를 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserBetStatusDto.class))),
            @ApiResponse(responseCode = "401", description = "인증 실패"),
            @ApiResponse(responseCode = "404", description = "베팅 정보 없음")
    })
    @GetMapping("/games/{gameId}/my-status")
    public ResponseEntity<UserBetStatusDto> getMyBetStatus(
            @Parameter(description = "조회할 게임 ID", example = "123", required = true)
            @PathVariable Long gameId,
            Authentication authentication) {

        String username = authentication.getName();
        Long userId = userService.getUserIdByUsername(username);

        UserBetStatusDto status = bettingService.getUserBetStatus(userId, gameId);
        return ResponseEntity.ok(status);
    }

    @Operation(
            summary = "관리자용 베팅 정산",
            description = "관리자가 게임 종료 후 우승팀 ID를 넘기면 베팅을 정산합니다. `winnerTeamId`가 없으면 환불 처리됩니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "정산 또는 환불 완료",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "관리자 권한 필요"),
            @ApiResponse(responseCode = "404", description = "게임 정보 없음")
    })
    @PostMapping("/games/{gameId}/settle")
    public ResponseEntity<String> settleBets(
            @Parameter(description = "정산할 게임 ID", example = "123", required = true)
            @PathVariable Long gameId,
            @Parameter(description = "우승 팀 ID (미전달 시 환불)", example = "10")
            @RequestParam(required = false) Long winnerTeamId) {

        bettingService.settleBets(gameId, winnerTeamId);
        if (winnerTeamId == null) {
            return ResponseEntity.ok("베팅 취소 및 환불이 완료되었습니다");
        } else {
            return ResponseEntity.ok("베팅 정산이 완료되었습니다");
        }
    }
}
