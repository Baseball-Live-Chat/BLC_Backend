package com.blc.blc_backend.chat.repository;

import com.blc.blc_backend.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    // 기존 메서드 (모든 메시지 조회)
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

    // 🆕 최근 메시지만 조회하는 메서드 추가
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findTopMessagesByRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId, Pageable pageable);

    @Query("""
        SELECT cm.roomId   AS roomId,
               cm.teamId   AS teamId,
               COUNT(cm)   AS cnt
        FROM ChatMessage cm
        WHERE cm.roomId IN :roomIds
        GROUP BY cm.roomId, cm.teamId
    """)
    List<RoomTeamCount> countByRoomIds(@Param("roomIds") List<Long> roomIds);

    interface RoomTeamCount {
        Long getRoomId();
        Long getTeamId();
        Long getCnt();
    }
}