package com.blc.blc_backend.betting.dto;

public interface BettingStatsProjection {
    Long getHomePoints();
    Long getAwayPoints();
    Integer getHomeCount();
    Integer getAwayCount();
    Long getTotalPoints();
    Integer getTotalCount();
}