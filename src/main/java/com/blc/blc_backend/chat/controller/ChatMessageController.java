package com.blc.blc_backend.chat.controller;


import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.dto.RoomCountResponse;
import com.blc.blc_backend.chat.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
@Slf4j
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessageResponseDto> postMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageRequestDto dto) {
        ChatMessageResponseDto created = chatMessageService.createMessage(roomId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<ChatMessageResponseDto>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(value = "limit", defaultValue = "20") int limit,
            @RequestParam(value = "all", defaultValue = "false") boolean all) {

        log.debug("채팅 메시지 조회 요청 → roomId={}, limit={}, all={}", roomId, limit, all);

        try {
            List<ChatMessageResponseDto> messages;

            if (all) {
                // 모든 메시지 조회 (기존 방식)
                messages = chatMessageService.getMessagesByRoom(roomId);
                log.debug("전체 메시지 조회 완료 → roomId={}, count={}", roomId, messages.size());
            } else {
                // 🆕 최근 메시지만 조회 (새로운 기본 방식)
                messages = chatMessageService.getRecentMessagesByRoom(roomId, limit);
                log.debug("최근 메시지 조회 완료 → roomId={}, limit={}, count={}", roomId, limit, messages.size());
            }

            if (messages.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(messages);

        } catch (Exception e) {
            log.error("채팅 메시지 조회 실패 → roomId={}", roomId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/rooms/counts")
    public List<RoomCountResponse> getCounts(
            @RequestParam List<Long> roomIds
    ) {
        return chatMessageService.getCountsForRooms(roomIds);
    }
}