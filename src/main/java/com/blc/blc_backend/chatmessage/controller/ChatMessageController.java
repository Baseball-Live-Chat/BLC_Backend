package com.blc.blc_backend.chatmessage.controller;


import com.blc.blc_backend.chatmessage.dto.ChatMessageRequestDto;
import com.blc.blc_backend.chatmessage.entity.ChatMessage;
import com.blc.blc_backend.chatmessage.service.ChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatMessageController {
    private final ChatMessageService chatMessageService;

    @PostMapping("/rooms/{roomId}/messages")
    public ResponseEntity<ChatMessage> postMessage(
            @PathVariable Long roomId,
            @RequestBody ChatMessageRequestDto dto) {
        ChatMessage created = chatMessageService.createMessage(roomId, dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<ChatMessage>> getMessages(@PathVariable Long roomId) {
        List<ChatMessage> messages = chatMessageService.getMessagesByRoom(roomId);
        if (messages.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(messages);
    }
}