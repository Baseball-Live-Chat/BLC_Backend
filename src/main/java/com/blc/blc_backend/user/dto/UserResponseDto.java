package com.blc.blc_backend.user.dto;

import com.blc.blc_backend.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class UserResponseDto {
    private Long userId;
    private String username;
    private String email;
    private String nickname;
    private String profileImageUrl;
    private Long favoriteTeamId;
    private Long points;                // 보유 포인트
    private UserRole userRole;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
