package com.blc.blc_backend.chatroom.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRequest {
    @NotBlank
    private String roomName;
    private Boolean isActive;
    private Integer maxParticipants;
}
