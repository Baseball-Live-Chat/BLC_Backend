package com.blc.blc_backend.game.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameListResponse {
    private final List<GameInfo> games;
    private final int currentPage;
    private final int totalPages;
    private final long totalCount;
    private final int pageSize;
    private final boolean hasNext;
    private final boolean hasPrevious;
}