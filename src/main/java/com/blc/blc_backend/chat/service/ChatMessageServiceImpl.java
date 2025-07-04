package com.blc.blc_backend.chat.service;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.dto.RoomCountResponse;
import com.blc.blc_backend.chat.entity.ChatMessage;
import com.blc.blc_backend.chat.repository.ChatMessageRepository;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.service.UserService;
import com.blc.blc_backend.chat.repository.ChatMessageRepository.RoomTeamCount;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ChatMessageServiceImpl implements ChatMessageService {
    private static final String ANONYMOUS_NICKNAME = "익명";
    private final ChatMessageRepository repository;
    private final UserService userService;

    public List<RoomCountResponse> getCountsForRooms(List<Long> roomIds) {
        // 초기화
        Map<Long, RoomCountResponse> map = roomIds.stream()
                .collect(Collectors.toMap(id -> id, id -> new RoomCountResponse(id, 0L, 0L)));

        // roomId x teamId(1=home,2=away)별 집계
        List<RoomTeamCount> raw = repository.countByRoomIds(roomIds);
        for (var rtc : raw) {
            RoomCountResponse resp = map.get(rtc.getRoomId());
            if (rtc.getTeamId() == 1L) {
                resp.setHomeCount(rtc.getCnt());
            } else if (rtc.getTeamId() == 2L) {
                resp.setAwayCount(rtc.getCnt());
            }
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public ChatMessageResponseDto createMessage(Long roomId, ChatMessageRequestDto dto) {
        Long userId = dto.getUserId();

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
        List<ChatMessage > messages = repository.findByRoomIdOrderByCreatedAtAsc(roomId);
        log.debug("채팅 메시지 조회 → roomId={}, count={}", roomId, messages.size());
        return messages.stream()
                .map(this::makeChatMessageResponse)
                .collect(Collectors.toList());
    }
}
