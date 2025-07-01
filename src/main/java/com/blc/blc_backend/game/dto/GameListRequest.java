package com.blc.blc_backend.game.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
public class GameListRequest {
    private int page = 1;
    private int size = 10;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate startDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate endDate;

    private String teamCode; // 특정 팀의 경기만 조회
    private String stadium;  // 특정 구장의 경기만 조회

    // 페이징을 위한 offset 계산
    public int getOffset() {
        return (page - 1) * size;
    }
}