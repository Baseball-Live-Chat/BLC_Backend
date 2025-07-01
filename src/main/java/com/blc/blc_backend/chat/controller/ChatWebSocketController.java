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

    @MessageMapping("/chat.sendMessage/{gameId}")
    @SendTo("/topic/game/{gameId}")
    public ChatMessageResponseDto  sendMessage(@DestinationVariable Long gameId,@Payload ChatMessageRequestDto messageRequest) {
        Long roomId = chatRoomService.getRoomIdByGameId(gameId);
        return chatMessageService.createMessage(roomId, messageRequest);
    }

    @MessageMapping("/chat.join/{roomId}") //채팅방 참여 인원 체크로 필요할 수도?
    public void joinChat(@DestinationVariable String roomId, @Payload String message) {

    }

}
