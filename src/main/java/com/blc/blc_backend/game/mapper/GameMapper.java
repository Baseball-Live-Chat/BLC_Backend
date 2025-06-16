package com.blc.blc_backend.game.mapper;

import com.blc.blc_backend.game.dto.GameInfo;
import com.blc.blc_backend.game.dto.GameDetailInfo;
import com.blc.blc_backend.game.dto.GameListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface GameMapper {

    /**
     * 전체 경기 리스트 조회 (페이징, 필터링)
     */
    List<GameInfo> findGames(GameListRequest request);

    /**
     * 전체 경기 수 조회 (필터링 조건 포함)
     */
    long countGames(GameListRequest request);

    /**
     * 특정 경기 상세 조회 (ID 기준) - 상세 정보 포함
     */
    GameDetailInfo findGameDetailById(@Param("gameId") Long gameId);
}