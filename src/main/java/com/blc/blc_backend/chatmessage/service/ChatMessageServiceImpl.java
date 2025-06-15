package com.blc.blc_backend.chatmessage.service;

import com.blc.blc_backend.chatmessage.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chatmessage.entity.ChatMessage;
import com.blc.blc_backend.chatmessage.repository.ChatMessageRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {
    private final ChatMessageRepository repository;

    @Override
    public ChatMessage createMessage(Long roomId, ChatMessageRequestDto dto) {
        ChatMessage msg = new ChatMessage();
        msg.setRoomId(roomId);
        msg.setUserId(dto.getUserId());
        msg.setMessageContent(dto.getContent());
        msg.setMessageType(dto.getType());

        ChatMessage saved = repository.save(msg);
        // @Slf4j가 제공하는 log 사용
        log.info("새로운 채팅 메시지 생성 → roomId={}, userId={}, type={}, content='{}'",
                roomId, dto.getUserId(), dto.getType(), dto.getContent());
        return saved;
    }

    @Override
    public List<ChatMessage> getMessagesByRoom(Long roomId) {
        List<ChatMessage> list = repository.findByRoomIdOrderByCreatedAtAsc(roomId);
        log.info("채팅 메시지 조회 → roomId={}, count={}", roomId, list.size());
        return list;
    }
}
