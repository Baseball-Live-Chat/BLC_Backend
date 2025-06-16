package com.blc.blc_backend.users.dto;

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
