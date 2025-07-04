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

    @MessageMapping("/chat.sendMessage/{gameId}")
    @SendTo("/topic/game/{gameId}")
    public ChatMessageResponseDto sendMessage(
            @DestinationVariable Long gameId,
            @Payload ChatMessageRequestDto messageRequest
    ) {
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

    @MessageMapping("/chat.join/{roomId}") //채팅방 참여 인원 체크로 필요할 수도?
    public void joinChat(@DestinationVariable String roomId, @Payload String message) {

    }

}
