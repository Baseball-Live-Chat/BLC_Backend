package com.blc.blc_backend.chatroom.repository;

import com.blc.blc_backend.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByGame_GameId(Long gameId);
    List<ChatRoom> findByIsActiveTrue();
    /**
     * 지정된 기준일 이전 게임에 속하면서, 아직 활성화(isActive=true)된 채팅방을 모두 조회
     */
    List<ChatRoom> findByGame_GameDateBeforeAndIsActiveTrue(LocalDateTime before);

    /**
     * 경기 id로 채팅방 데이터 조회
     * @param gameId
     * @return room
     */
    ChatRoom findChatRoomByGame_GameId(Long gameId);
}

