package com.blc.blc_backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CountsDeltaDto {
    private Long roomId;
    private String type; // "home" 또는 "away"
}
