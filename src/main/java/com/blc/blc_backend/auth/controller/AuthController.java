package com.blc.blc_backend.auth.controller;

import com.blc.blc_backend.auth.security.FirebaseAuthenticationToken;
import com.blc.blc_backend.auth.service.UserSyncService;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.entity.User;
import com.blc.blc_backend.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    // Firebase Token 기반으로 변경
    private final UserSyncService userSyncService;
    private final UserRepository userRepository;

    @PostMapping("/sync")
    public ResponseEntity<?> syncUserProfile(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "");
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            
            User user = userSyncService.syncUserFromFirebase(
                    decodedToken.getUid(),
                    decodedToken.getEmail(),
                    decodedToken.getName()
            );
            
            return ResponseEntity.ok(UserResponseDto.builder()
                    .userId(user.getUserId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .profileImageUrl(user.getProfileImageUrl())
                    .favoriteTeamId(user.getFavoriteTeamId())
                    .build());
                    
        } catch (FirebaseAuthException e) {
            log.error("Firebase token verification failed", e);
            return ResponseEntity.status(401).body("Invalid token");
        } catch (Exception e) {
            log.error("User sync failed", e);
            return ResponseEntity.status(500).body("User sync failed");
        }
    }

    @GetMapping("/users/profile")
    public ResponseEntity<?> getUserProfile(Authentication auth) {
        if (!(auth instanceof FirebaseAuthenticationToken)) {
            return ResponseEntity.status(401).body("Invalid authentication");
        }
        
        FirebaseAuthenticationToken firebaseAuth = (FirebaseAuthenticationToken) auth;
        User user = userRepository.findByFirebaseUid(firebaseAuth.getUid()).orElse(null);
        
        if (user == null) {
            return ResponseEntity.status(404).body("User not found");
        }
        
        return ResponseEntity.ok(UserResponseDto.builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .favoriteTeamId(user.getFavoriteTeamId())
                .build());
    }
}