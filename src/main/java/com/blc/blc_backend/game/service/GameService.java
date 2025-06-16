package com.blc.blc_backend.game.service;

import com.blc.blc_backend.game.dto.GameInfo;
import com.blc.blc_backend.game.dto.GameDetailInfo;
import com.blc.blc_backend.game.dto.GameListRequest;
import com.blc.blc_backend.game.dto.GameListResponse;
import com.blc.blc_backend.game.mapper.GameMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameMapper gameMapper;

    /**
     * 전체 경기 리스트 조회 (페이징, 필터링)
     */
    public GameListResponse getGames(GameListRequest request) {
        List<GameInfo> games = gameMapper.findGames(request);
        long totalCount = gameMapper.countGames(request);

        int totalPages = (int) Math.ceil((double) totalCount / request.getSize());

        return GameListResponse.builder()
                .games(games)
                .currentPage(request.getPage())
                .totalPages(totalPages)
                .totalCount(totalCount)
                .pageSize(request.getSize())
                .hasNext(request.getPage() < totalPages)
                .hasPrevious(request.getPage() > 1)
                .build();
    }

    /**
     * 특정 경기 상세 조회
     */
    public GameDetailInfo getGameDetailById(Long gameId) {
        GameDetailInfo gameDetailInfo = gameMapper.findGameDetailById(gameId);
        if (gameDetailInfo == null) {
            throw new IllegalArgumentException("해당 경기를 찾을 수 없습니다. ID: " + gameId);
        }
        return gameDetailInfo;
    }
}