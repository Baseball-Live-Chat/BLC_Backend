package com.blc.blc_backend.auth.controller;

import com.blc.blc_backend.auth.dto.LoginRequestDto;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;

    /**
     * 로그인
     * 요청 바디에 email, password를 받고,
     * 성공 시 세션 생성 → JSESSIONID 쿠키 자동 발급
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto req,
                                   HttpServletRequest httpReq) {
        try {
            // principal로 email 사용
            UsernamePasswordAuthenticationToken token =
                    new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword());

            Authentication auth = authenticationManager.authenticate(token);
            SecurityContextHolder.getContext().setAuthentication(auth);

            // 세션 생성 (없으면 새로 만든 뒤 JSESSIONID 발급)
            HttpSession session = httpReq.getSession(true);

            return ResponseEntity.ok("로그인 성공");
        } catch (BadCredentialsException ex) {
            // 인증 실패
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    /**
     * 로그아웃
     * 세션 무효화하고 SecurityContext 비움
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest httpReq) {
        HttpSession session = httpReq.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return ResponseEntity.ok("로그아웃 성공");
    }
}
