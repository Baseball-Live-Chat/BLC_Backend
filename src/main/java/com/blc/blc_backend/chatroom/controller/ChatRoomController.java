package com.blc.blc_backend.chatroom.controller;

import com.blc.blc_backend.chatroom.dto.ChatRoomRequest;
import com.blc.blc_backend.chatroom.dto.ChatRoomResponse;
import com.blc.blc_backend.chatroom.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 조회·생성·수정·삭제 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @Operation(summary = "전체 활성 채팅방 조회",
            description = "isActive=true인 모든 채팅방의 목록을 반환합니다.")
    @GetMapping("/api/chat-rooms")
    public List<ChatRoomResponse> getAllActive() {
        return chatRoomService.getAllActive();
    }

    // 예시: 추가 엔드포인트에도 동일하게 @Operation 붙이기
    @Operation(summary = "채팅방 단건 조회",
            description = "roomId에 해당하는 채팅방 정보를 반환합니다.")
    @GetMapping("/api/chat-rooms/{id}")
    public ChatRoomResponse getById(@PathVariable Long id) {
        return chatRoomService.getById(id);
    }

    //-------------------------- 관리자 기능 --------------------------
    // TODO: AUTH 연동 시, security 이용 role check로 admin만 사용가능하게 권한 수정 필요
    @Operation(summary = "채팅방 생성",
            description = "gameId와 roomName을 받아 새로운 채팅방을 만듭니다.")
    @PostMapping("/admin/chat-rooms")
    public ChatRoomResponse create(
            @RequestParam Long gameId,
            @RequestBody ChatRoomRequest req) {
        return chatRoomService.create(gameId, req.getRoomName());
    }

    @Operation(summary = "채팅방 수정",
            description = "roomId에 해당하는 채팅방 정보를 업데이트합니다.(관리자용)")
    @PutMapping("/admin/chat-rooms/{id}")
    public ChatRoomResponse update(
            @PathVariable Long id,
            @RequestBody ChatRoomRequest req) {
        return chatRoomService.update(id, req);
    }

    @Operation(summary = "채팅방 삭제",
            description = "roomId에 해당하는 채팅방을 삭제합니다.")
    @DeleteMapping("/admin/chat-rooms/{id}")
    public void delete(@PathVariable Long id) {
        chatRoomService.delete(id);
    }
}
