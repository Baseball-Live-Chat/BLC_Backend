package com.blc.blc_backend.chatmessage.dto;

import com.blc.blc_backend.chatmessage.entity.MessageType;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChatMessageRequestDto {
    private Long userId;
    private String content;
    private MessageType type;
}
