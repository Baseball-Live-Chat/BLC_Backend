package com.blc.blc_backend.auth.service;

import com.blc.blc_backend.user.entity.User;
import com.blc.blc_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSyncService {
    // Firebase Token 기반으로 변경
    private final UserRepository userRepository;

    @Transactional
    public User syncUserFromFirebase(String firebaseUid, String email, String displayName) {
        User user = userRepository.findByFirebaseUid(firebaseUid).orElse(null);

        if (user == null) {
            // 새 사용자 생성
            user = User.builder()
                    .firebaseUid(firebaseUid)
                    .email(email)
                    .nickname(displayName != null ? displayName : extractUsernameFromEmail(email))
                    .createdAt(LocalDateTime.now())
                    .build();
            
            log.info("Creating new user with Firebase UID: {}", firebaseUid);
        } else {
            // 기존 사용자 정보 업데이트
            user.setEmail(email);
            if (displayName != null) {
                user.setNickname(displayName);
            }
            user.setUpdatedAt(LocalDateTime.now());
            
            log.info("Updating existing user with Firebase UID: {}", firebaseUid);
        }

        return userRepository.save(user);
    }

    private String extractUsernameFromEmail(String email) {
        if (email != null && email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "user_" + System.currentTimeMillis();
    }
}