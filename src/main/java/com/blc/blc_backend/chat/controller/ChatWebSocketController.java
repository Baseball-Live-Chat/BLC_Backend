package com.blc.blc_backend.chat.controller;

import com.blc.blc_backend.chat.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chat.dto.ChatMessageResponseDto;
import com.blc.blc_backend.chat.dto.CountsDeltaDto;
import com.blc.blc_backend.chat.service.ChatMessageService;
import com.blc.blc_backend.chatroom.service.ChatRoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final ChatRoomService chatRoomService;
    private final SimpMessagingTemplate simpMessagingTemplate;  // 추가

    /**
     * 경기별 채팅방 메시지 전송
     * /app/chat.sendMessage/{gameId}
     */
    @MessageMapping("/chat.sendMessage/{gameId}")
    @SendTo("/topic/game/{gameId}")
    public ChatMessageResponseDto sendMessage(
            @DestinationVariable Long gameId,
            @Payload ChatMessageRequestDto messageRequest
    ) {
  
        log.debug("경기별 채팅방 메시지 수신: gameId={}, teamId={}, content='{}'",
                gameId, messageRequest.getTeamId(), messageRequest.getContent());
        // 1) 메시지 저장 & DTO 변환
        ChatMessageResponseDto msg = chatMessageService.createMessage(
                chatRoomService.getRoomIdByGameId(gameId),
                messageRequest
        );

        // 2) teamId → home/away 타입 매핑
        String type = (msg.getTeamId() != null && msg.getTeamId() == 1L)
                ? "home"
                : "away";

        // 3) 델타만 브로드캐스트
        CountsDeltaDto delta = new CountsDeltaDto(msg.getRoomId(), type);
        simpMessagingTemplate.convertAndSend(
                "/topic/game/" + gameId + "/counts-delta",
                delta
        );

        // 4) 원래 구독자에게도 ChatMessageResponseDto 전송
        return msg;
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