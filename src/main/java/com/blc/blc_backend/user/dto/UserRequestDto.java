package com.blc.blc_backend.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {
    private String username;
    private String email;
    private String password;
    private String nickname;
    private String profileImageUrl;
    private Long favoriteTeamId;
}
