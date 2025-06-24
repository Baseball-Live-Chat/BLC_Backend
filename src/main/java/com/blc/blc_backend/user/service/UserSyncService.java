package com.blc.blc_backend.user.service;

import com.blc.blc_backend.user.entity.User;
import com.blc.blc_backend.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Firebase 사용자 정보와 로컬 데이터베이스 사용자 정보를 동기화하는 서비스
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class UserSyncService {

    private final UserRepository userRepository;

    /**
     * Firebase 토큰을 기반으로 사용자 정보를 동기화하고 User 엔티티를 반환
     * 
     * @param firebaseToken Firebase에서 검증된 토큰
     * @return 동기화된 User 엔티티
     */
    @Transactional
    public User syncUser(FirebaseToken firebaseToken) {
        String firebaseUid = firebaseToken.getUid();
        String email = firebaseToken.getEmail();
        String name = firebaseToken.getName();

        log.debug("Syncing user: uid={}, email={}, name={}", firebaseUid, email, name);

        // 기존 사용자 확인
        Optional<User> existingUser = userRepository.findByFirebaseUid(firebaseUid);
        
        if (existingUser.isPresent()) {
            // 기존 사용자 정보 업데이트
            User user = existingUser.get();
            boolean updated = false;

            // 이메일 업데이트 (Firebase에서 변경된 경우)
            if (email != null && !email.equals(user.getEmail())) {
                user.setEmail(email);
                updated = true;
            }

            // 이름 업데이트 (Firebase에서 변경된 경우)
            if (name != null && !name.equals(user.getNickname())) {
                user.setNickname(name);
                updated = true;
            }

            if (updated) {
                user.setModifiedAt(LocalDateTime.now());
                user = userRepository.save(user);
                log.debug("Updated existing user: {}", user.getId());
            }

            return user;
        } else {
            // 신규 사용자 생성
            User newUser = User.builder()
                    .firebaseUid(firebaseUid)
                    .email(email)
                    .nickname(name != null ? name : email) // 이름이 없으면 이메일 사용
                    .createdAt(LocalDateTime.now())
                    .modifiedAt(LocalDateTime.now())
                    .build();

            newUser = userRepository.save(newUser);
            log.info("Created new user: {} (Firebase UID: {})", newUser.getId(), firebaseUid);
            
            return newUser;
        }
    }

    /**
     * Firebase UID로 사용자를 조회
     * 
     * @param firebaseUid Firebase 사용자 UID
     * @return User 엔티티 (Optional)
     */
    public Optional<User> findByFirebaseUid(String firebaseUid) {
        return userRepository.findByFirebaseUid(firebaseUid);
    }

    /**
     * 사용자 정보를 강제로 다시 동기화 (관리자 기능)
     * 
     * @param firebaseUid Firebase 사용자 UID
     * @param email 새로운 이메일
     * @param nickname 새로운 닉네임
     * @return 업데이트된 User 엔티티
     */
    @Transactional
    public Optional<User> forceUpdateUser(String firebaseUid, String email, String nickname) {
        Optional<User> userOpt = userRepository.findByFirebaseUid(firebaseUid);
        
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            
            if (email != null) {
                user.setEmail(email);
            }
            
            if (nickname != null) {
                user.setNickname(nickname);
            }
            
            user.setModifiedAt(LocalDateTime.now());
            user = userRepository.save(user);
            
            log.info("Force updated user: {} (Firebase UID: {})", user.getId(), firebaseUid);
            return Optional.of(user);
        }
        
        return Optional.empty();
    }
}