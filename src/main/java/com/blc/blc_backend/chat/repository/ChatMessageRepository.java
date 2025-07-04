package com.blc.blc_backend.chat.repository;

import com.blc.blc_backend.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);

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