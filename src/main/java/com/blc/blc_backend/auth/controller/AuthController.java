package com.blc.blc_backend.auth.controller;

import com.blc.blc_backend.auth.dto.LoginRequestDto;
import com.blc.blc_backend.auth.service.AuthService;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.service.UserService;
import com.blc.blc_backend.userAttendance.Service.UserAttendanceService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;
    private final UserAttendanceService attendanceService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto req,
                                        HttpServletRequest httpReq) {
        authService.login(req, httpReq);  // 예외가 여기서 throw 되면 어드바이스로 넘어감

        // 로그인 한 유저정보 출석 시키기
        UserResponseDto user = userService.getUserByUsername(req.getUsername());
        attendanceService.recordAttendance(user.getUserId());

        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest httpReq) {
        authService.logout(httpReq);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(Authentication authentication) {
        String username = authentication.getName();
        UserResponseDto dto = userService.getUserByUsername(username);
        return ResponseEntity.ok(dto);
    }
}
