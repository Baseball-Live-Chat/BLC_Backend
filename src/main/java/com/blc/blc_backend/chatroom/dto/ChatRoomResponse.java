package com.blc.blc_backend.chatroom.dto;

import com.blc.blc_backend.chatroom.entity.ChatRoom;
import lombok.Getter;

@Getter
public class ChatRoomResponse {
    private Long roomId;
    private Long gameId;
    private String roomName;
    private Boolean isActive;
    private Integer maxParticipants;

    public static ChatRoomResponse of(ChatRoom room) {
        ChatRoomResponse dto = new ChatRoomResponse();
        dto.roomId = room.getRoomId();
        dto.gameId = room.getGame().getGameId();
        dto.roomName = room.getRoomName();
        dto.isActive = room.getIsActive();
        dto.maxParticipants = room.getMaxParticipants();
        return dto;
    }
}
