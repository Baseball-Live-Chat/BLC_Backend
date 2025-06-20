package com.blc.blc_backend.auth.service;

import com.blc.blc_backend.auth.dto.LoginRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;

    /**
     * 로그인 처리
     * - email/password 인증
     * - SecurityContext에 Authentication 저장
     * - HttpSession 생성 (JSESSIONID 발급)
     */
    public void login(LoginRequestDto req, HttpServletRequest httpReq) {
        UsernamePasswordAuthenticationToken token =
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword());

        Authentication auth = authenticationManager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 세션 생성 (없으면 새로 만들고 JSESSIONID 발급)
        httpReq.getSession(true);
    }

    /**
     * 로그아웃 처리
     * - 세션 무효화
     * - SecurityContext 초기화
     */
    public void logout(HttpServletRequest httpReq) {
        HttpSession session = httpReq.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }
}
