package com.blc.blc_backend.chat.dto;

import com.blc.blc_backend.chat.entity.ChatMessage;
import com.blc.blc_backend.chat.entity.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ChatMessageResponseDto {
    private Long userId;
    private String nickname;
    private Long teamId;
    private String content;
    private MessageType type;
    private LocalDateTime createdAt;

    public ChatMessageResponseDto(ChatMessage entity, String nickname) {
        this.userId = entity.getUserId();
        this.nickname = nickname;
        this.teamId = entity.getTeamId();
        this.content = entity.getMessageContent();
        this.type = entity.getMessageType();
        this.createdAt = entity.getCreatedAt();
    }
}
