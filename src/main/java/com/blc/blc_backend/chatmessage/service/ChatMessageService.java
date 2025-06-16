package com.blc.blc_backend.chatmessage.service;

import com.blc.blc_backend.chatmessage.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chatmessage.entity.ChatMessage;

import java.util.List;

public interface ChatMessageService {
    //Create: 채팅 메시지 생성
    ChatMessage createMessage(Long roomId, ChatMessageRequestDto dto);
    //Read: 해당 방의 모든 메시지 조회 (시간 순)
    List<ChatMessage> getMessagesByRoom(Long roomId);
}
