package com.blc.blc_backend.chatroom.entity;

import com.blc.blc_backend.game.entity.Game;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roomId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false, unique = true)
    private Game game;

    @Column(nullable = false, length = 200)
    private String roomName;

    @Column(nullable = false)
    private Boolean isActive = true;

    private Integer maxParticipants = 10000;

    @Builder
    public ChatRoom(Game game, String roomName, Boolean isActive, Integer maxParticipants) {
        this.game = game;
        this.roomName = roomName;
        this.isActive = isActive;
        this.maxParticipants = maxParticipants;
    }

    public void updateRoomName(String roomName) {
        this.roomName = roomName;
    }

    public void updateMaxParticipants(Integer maxParticipants) {
        this.maxParticipants = maxParticipants;
    }

    public void updateIsActive(Boolean isActive) {
        this.isActive = isActive;
    }
}
