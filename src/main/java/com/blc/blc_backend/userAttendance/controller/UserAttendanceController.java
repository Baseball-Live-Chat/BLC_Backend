package com.blc.blc_backend.userAttendance.controller;

import com.blc.blc_backend.userAttendance.Service.UserAttendanceService;
import com.blc.blc_backend.userAttendance.dto.UserAttendanceRequest;
import com.blc.blc_backend.userAttendance.dto.UserAttendanceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Vue.js 프론트엔드를 위한 CORS 설정
public class UserAttendanceController {

    private final UserAttendanceService userAttendanceService;

    /**
     * 특정 달 출석 날짜 조회
     * GET /api/attendance?year=2025&month=01&userName=kkh
     */
    @GetMapping
    public ResponseEntity<UserAttendanceResponse> getUserAttendance(
            UserAttendanceRequest request,
            Authentication authentication
        ) {
        try {
            if(authentication == null) throw new IllegalArgumentException("로그인 필요");
            String username = authentication.getName();
            UserAttendanceResponse response = userAttendanceService.getUserAttendance(request, username);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
