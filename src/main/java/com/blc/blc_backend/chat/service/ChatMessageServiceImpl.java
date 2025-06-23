package com.blc.blc_backend.chat.service;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.entity.ChatMessage;
import com.blc.blc_backend.chat.repository.ChatMessageRepository;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {
    private static final Long ANONYMOUS_ID = -1L;
    private static final String ANONYMOUS_NICKNAME = "익명";
    private final ChatMessageRepository repository;
    private final UserService userService;

    @Override
    public ChatMessageResponseDto createMessage(Long roomId, ChatMessageRequestDto dto) {
        Long userId = checkAnonymous(dto.getUserId());

        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setUserId(userId);
        msg.setMessageContent(dto.getContent());
        msg.setMessageType(dto.getType());
        msg.setTeamId(dto.getTeamId());

        ChatMessage saved = repository.save(msg);

        // @Slf4j가 제공하는 log 사용
        log.debug("채팅 메시지 생성 → roomId={}, userId={}, type={}, createdAt={}, teamId={}, 내용='{}'",
                roomId, dto.getUserId(), dto.getType(), saved.getCreatedAt(), dto.getTeamId(), dto.getContent());

        return makeChatMessageResponse(saved, userId);
    }

    private ChatMessageResponseDto makeChatMessageResponse(ChatMessage msg, Long userId) {
        if(userId == ANONYMOUS_ID) {
            return new ChatMessageResponseDto(msg, ANONYMOUS_NICKNAME);
        }
        UserResponseDto user = userService.getUserById(msg.getUserId());
        return new ChatMessageResponseDto(msg, user.getNickname());
    }

    // null userId → 익명 처리용 -1L
    private Long checkAnonymous(Long receivedId) {
        if(receivedId == null) {
            return ANONYMOUS_ID;
        }
        return receivedId;
    }

    @Override
    public List<ChatMessageResponseDto> getMessagesByRoom(Long roomId) {
        List<ChatMessage > messages = repository.findByRoomIdOrderByCreatedAtAsc(roomId);
        log.debug("채팅 메시지 조회 → roomId={}, count={}", roomId, messages.size());
        return messages.stream()
                .map(msg -> makeChatMessageResponse(msg, msg.getUserId()))
                .collect(Collectors.toList());
    }
}
