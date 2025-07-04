package com.blc.blc_backend.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RoomCountResponse {
    private Long roomId;
    private Long homeCount;
    private Long awayCount;
}
