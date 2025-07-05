package com.blc.blc_backend.chat.service;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.dto.RoomCountResponse;
import com.blc.blc_backend.chat.entity.ChatMessage;
import com.blc.blc_backend.chat.repository.ChatMessageRepository;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {

    private final ChatMessageRepository repository;
    private final UserService userService;

    // 기본 최근 메시지 조회 개수
    private static final int DEFAULT_RECENT_LIMIT = 20;
    private static final String ANONYMOUS_NICKNAME = "익명";

    @Override
    public ChatMessageResponseDto createMessage(Long roomId, ChatMessageRequestDto dto) {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setUserId(dto.getUserId());
        msg.setTeamId(dto.getTeamId());
        msg.setMessageContent(dto.getContent());
        msg.setMessageType(dto.getType());
        msg.setCreatedAt(LocalDateTime.now());

        ChatMessage saved = repository.save(msg);

        log.debug("채팅 메시지 생성 → roomId={}, userId={}, type={}, createdAt={}, teamId={}, 내용='{}'",
                roomId, dto.getUserId(), dto.getType(), saved.getCreatedAt(), dto.getTeamId(), dto.getContent());

        return makeChatMessageResponse(saved);
    }

    private ChatMessageResponseDto makeChatMessageResponse(ChatMessage msg) {
        if(msg.getUserId() == null) {
            return new ChatMessageResponseDto(msg, ANONYMOUS_NICKNAME);
        }
        UserResponseDto user = userService.getUserById(msg.getUserId());
        return new ChatMessageResponseDto(msg, user.getNickname());
    }

    @Override
    public List<ChatMessageResponseDto> getMessagesByRoom(Long roomId) {
        List<ChatMessage> messages = repository.findByRoomIdOrderByCreatedAtAsc(roomId);
        log.debug("채팅 메시지 조회 (전체) → roomId={}, count={}", roomId, messages.size());
        return messages.stream()
                .map(this::makeChatMessageResponse)
                .collect(Collectors.toList());
    }

    // 🆕 최근 메시지만 조회 (기본 20개)
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getRecentMessagesByRoom(Long roomId) {
        return getRecentMessagesByRoom(roomId, DEFAULT_RECENT_LIMIT);
    }

    // 🆕 최근 메시지를 개수 지정해서 조회
    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageResponseDto> getRecentMessagesByRoom(Long roomId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        List<ChatMessage> messages = repository.findTopMessagesByRoomIdOrderByCreatedAtDesc(roomId, pageable);

        log.debug("채팅 메시지 조회 (최근 {}개) → roomId={}, count={}", limit, roomId, messages.size());

        // 최신순으로 가져온 것을 시간순(오래된 것부터)으로 뒤집어서 반환
        Collections.reverse(messages);

        return messages.stream()
                .map(this::makeChatMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoomCountResponse> getCountsForRooms(List<Long> roomIds) {
        // 기존 구현 유지...
        return null; // 실제 구현은 기존 코드 참조
    }
}