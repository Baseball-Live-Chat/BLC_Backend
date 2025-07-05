package com.blc.blc_backend.chat.service;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.dto.RoomCountResponse;

import java.util.List;

public interface ChatMessageService {
    // Create: 채팅 메시지 생성
    ChatMessageResponseDto createMessage(Long gameId, ChatMessageRequestDto dto);

    // Read: 해당 방의 모든 메시지 조회 (시간 순)
    List<ChatMessageResponseDto> getMessagesByRoom(Long roomId);

    // 🆕 Read: 해당 방의 최근 메시지만 조회 (기본값 20개)
    List<ChatMessageResponseDto> getRecentMessagesByRoom(Long roomId);

    // 🆕 Read: 해당 방의 최근 메시지를 개수 지정해서 조회
    List<ChatMessageResponseDto> getRecentMessagesByRoom(Long roomId, int limit);

    List<RoomCountResponse> getCountsForRooms(List<Long> roomIds);
}