package com.blc.blc_backend.chatroom.dto;

import com.blc.blc_backend.game.dto.GameDetailInfo;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomDetailResponse {
    private final Long roomId;
    private final Long gameId;        // null이면 고정 채팅방
    private final String roomName;
    private final Boolean isActive;
    private final Integer maxParticipants;
    private final Boolean isGeneralRoom;  // 고정 채팅방 여부

    // 경기별 채팅방인 경우에만 포함
    private final GameDetailInfo gameInfo;
}