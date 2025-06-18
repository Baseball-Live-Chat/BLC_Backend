package com.blc.blc_backend.auth.controller;

import com.blc.blc_backend.auth.dto.LoginRequestDto;
import com.blc.blc_backend.auth.service.AuthService;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequestDto req,
                                        HttpServletRequest httpReq) {
        authService.login(req, httpReq);  // 예외가 여기서 throw 되면 어드바이스로 넘어감
        return ResponseEntity.ok("로그인 성공");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletRequest httpReq) {
        authService.logout(httpReq);
        return ResponseEntity.ok("로그아웃 성공");
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(Authentication authentication) {
        String email = authentication.getName();
        UserResponseDto dto = userService.getUserByEmail(email);
        return ResponseEntity.ok(dto);
    }
}
