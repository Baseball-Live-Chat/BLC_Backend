package com.blc.blc_backend.chat.dto;

import com.blc.blc_backend.chat.entity.MessageType;
import lombok.Getter;

@Getter
public class ChatMessageRequestDto {
    private Long userId;
    private Long teamId;
    private String content;
    private MessageType type;
}
