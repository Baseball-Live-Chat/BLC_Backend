package com.blc.blc_backend.chat.service;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;

import java.util.List;

public interface ChatMessageService {
    //Create: 채팅 메시지 생성
    ChatMessageResponseDto createMessage(Long roomId, ChatMessageRequestDto dto);
    //Read: 해당 방의 모든 메시지 조회 (시간 순)
    List<ChatMessageResponseDto > getMessagesByRoom(Long roomId);
}
