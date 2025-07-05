package com.blc.blc_backend.chatroom.repository;

import com.blc.blc_backend.chatroom.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByGame_GameId(Long gameId);

    // 수정: 고정 채팅방이 먼저 나오도록 정렬
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.isActive = true " +
            "ORDER BY CASE WHEN cr.game IS NULL THEN 0 ELSE 1 END, cr.roomId")
    List<ChatRoom> findByIsActiveTrue();

    // 추가: 고정 채팅방만 조회
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.game IS NULL AND cr.isActive = true")
    Optional<ChatRoom> findGeneralChatRoom();

    // 추가: 경기별 채팅방만 조회
    @Query("SELECT cr FROM ChatRoom cr WHERE cr.game IS NOT NULL AND cr.isActive = true")
    List<ChatRoom> findGameChatRooms();

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

    /**
     * 주어진 gameId 에 해당하는 활성화된(ChatRoom.isActive = true) 방이 존재하는지 여부
     * @param gameId 조회할 Game ID
     * @return 존재하면 true
     */
    boolean existsByGameIdAndIsActiveTrue(Long gameId);
}

