package com.blc.blc_backend.game.controller;

import com.blc.blc_backend.game.dto.GameDetailInfo;
import com.blc.blc_backend.game.dto.GameListRequest;
import com.blc.blc_backend.game.dto.GameListResponse;
import com.blc.blc_backend.game.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Vue.js 프론트엔드를 위한 CORS 설정
public class GameController {

    private final GameService gameService;

    /**
     * 전체 경기 리스트 조회 (페이징, 필터링)
     * GET /api/games?page=1&size=10&startDate=2024-01-01&endDate=2024-12-31&teamCode=KIA&stadium=잠실
     */
    @GetMapping
    public ResponseEntity<GameListResponse> getGames(GameListRequest request) {
        try {
            GameListResponse response = gameService.getGames(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 경기 상세 조회
     * GET /api/games/{gameId}
     */
    @GetMapping("/{gameId}")
    public ResponseEntity<GameDetailInfo> getGameById(@PathVariable Long gameId) {
        try {
            GameDetailInfo gameDetailInfo = gameService.getGameDetailById(gameId);
            return ResponseEntity.ok(gameDetailInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}