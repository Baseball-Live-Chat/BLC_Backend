package com.blc.blc_backend.chat.controller;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.service.ChatMessageService;
import com.blc.blc_backend.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;

    /**
     * 경기별 채팅방 메시지 전송
     * /app/chat.sendMessage/{gameId}
     */
    @MessageMapping("/chat.sendMessage/{gameId}")
    @SendTo("/topic/game/{gameId}")
    public ChatMessageResponseDto sendGameMessage(
            @DestinationVariable Long gameId,
            @Payload ChatMessageRequestDto messageRequest) {

        log.debug("경기별 채팅방 메시지 수신: gameId={}, teamId={}, content='{}'",
                gameId, messageRequest.getTeamId(), messageRequest.getContent());

        try {
            Long roomId = chatRoomService.getRoomIdByGameId(gameId);
            return chatMessageService.createMessage(roomId, messageRequest);
        } catch (Exception e) {
            log.error("경기별 채팅방 메시지 처리 실패: gameId={}", gameId, e);
            throw e;
        }
    }

    /**
     * 🆕 고정 채팅방 메시지 전송
     * /app/chat.sendMessage/room/{roomId}
     */
    @MessageMapping("/chat.sendMessage/room/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public ChatMessageResponseDto sendRoomMessage(
            @DestinationVariable Long roomId,
            @Payload ChatMessageRequestDto messageRequest) {

        log.debug("고정 채팅방 메시지 수신: roomId={}, teamId={}, content='{}'",
                roomId, messageRequest.getTeamId(), messageRequest.getContent());

        try {
            return chatMessageService.createMessage(roomId, messageRequest);
        } catch (Exception e) {
            log.error("고정 채팅방 메시지 처리 실패: roomId={}", roomId, e);
            throw e;
        }
    }

    @MessageMapping("/chat.join/{roomId}")
    public void joinChat(@DestinationVariable String roomId, @Payload String message) {
        // 채팅방 참여 로직 (필요시 구현)
    }
}