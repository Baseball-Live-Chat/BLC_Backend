package com.blc.blc_backend.chat.service;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.entity.ChatMessage;
import com.blc.blc_backend.chat.repository.ChatMessageRepository;
import com.blc.blc_backend.users.dto.UserResponseDto;
import com.blc.blc_backend.users.service.UserService;
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
    private final ChatMessageRepository repository;
    private final UserService userService;

    @Override
    public ChatMessageResponseDto createMessage(Long roomId, ChatMessageRequestDto dto) {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setUserId(dto.getUserId());
        msg.setMessageContent(dto.getContent());
        msg.setMessageType(dto.getType());
        msg.setTeamId(dto.getTeamId());

        ChatMessage saved = repository.save(msg);

        UserResponseDto user = userService.getUserById(saved.getUserId());
        // @Slf4j가 제공하는 log 사용
        log.debug("채팅 메시지 생성 → roomId={}, userId={}, type={}, createdAt={}, teamId={}, 내용='{}'",
                roomId, dto.getUserId(), dto.getType(), saved.getCreatedAt(), dto.getTeamId(), dto.getContent());

        return new ChatMessageResponseDto(saved, user.getNickname());
    }

    @Override
    public List<ChatMessageResponseDto > getMessagesByRoom(Long roomId) {
        List<ChatMessage > messages = repository.findByRoomIdOrderByCreatedAtAsc(roomId);
        log.debug("채팅 메시지 조회 → roomId={}, count={}", roomId, messages.size());
        return messages.stream()
                .map(msg -> {
                    UserResponseDto user = userService.getUserById(msg.getUserId());
                    return new ChatMessageResponseDto(msg, user.getNickname());
                })
                .collect(Collectors.toList());
    }
}
