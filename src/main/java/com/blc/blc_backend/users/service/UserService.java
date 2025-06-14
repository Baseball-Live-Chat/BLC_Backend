package com.blc.blc_backend.users.service;

import com.blc.blc_backend.users.dto.UserRequestDto;
import com.blc.blc_backend.users.dto.UserResponseDto;
import com.blc.blc_backend.users.entity.User;
import com.blc.blc_backend.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 회원가입
     */
    @Transactional
    public UserResponseDto createUser(UserRequestDto dto) {
        // 중복 검사
        userRepository.findByUsername(dto.getUsername())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 아이디입니다."); });
        userRepository.findByEmail(dto.getEmail())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 등록된 이메일입니다."); });
        userRepository.findByNickname(dto.getNickname())
                .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 닉네임입니다."); });

        // 비밀번호 암호화
        String encodedPwd = passwordEncoder.encode(dto.getPassword());

        // Entity 생성 후 저장
        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(encodedPwd)
                .nickname(dto.getNickname())
                .profileImageUrl(dto.getProfileImageUrl())
                .favoriteTeamId(dto.getFavoriteTeamId())
                .build();

        User saved = userRepository.save(user);
        return toResponseDto(saved);
    }

    /**
     * 단건 조회
     */
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id=" + userId));
        return toResponseDto(user);
    }

    /**
     * 전체 조회
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * 정보 수정
     */
    @Transactional
    public UserResponseDto updateUser(Long userId, UserRequestDto dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다. id=" + userId));

        // 필드 업데이트
        if (!user.getUsername().equals(dto.getUsername())) {
            userRepository.findByUsername(dto.getUsername())
                    .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 아이디입니다."); });
            user.setUsername(dto.getUsername());
        }
        if (!user.getEmail().equals(dto.getEmail())) {
            userRepository.findByEmail(dto.getEmail())
                    .ifPresent(u -> { throw new IllegalArgumentException("이미 등록된 이메일입니다."); });
            user.setEmail(dto.getEmail());
        }
        if (!user.getNickname().equals(dto.getNickname())) {
            userRepository.findByNickname(dto.getNickname())
                    .ifPresent(u -> { throw new IllegalArgumentException("이미 사용 중인 닉네임입니다."); });
            user.setNickname(dto.getNickname());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        }
        user.setProfileImageUrl(dto.getProfileImageUrl());
        user.setFavoriteTeamId(dto.getFavoriteTeamId());

        // 변경 감지로 자동 업데이트
        return toResponseDto(user);
    }

    /**
     * 삭제
     */
    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("삭제할 유저를 찾을 수 없습니다. id=" + userId);
        }
        userRepository.deleteById(userId);
    }

    // Entity → DTO 변환 헬퍼
    private UserResponseDto toResponseDto(User user) {
        return UserResponseDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileImageUrl(user.getProfileImageUrl())
                .favoriteTeamId(user.getFavoriteTeamId())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    /**
     * 프론트 비동기 중복 체크용 메서드
     * - username/email/nickname 별로 호출
     * - 중복 시 ResourceConflictException 발생 (HTTP 409)
     */
    @Transactional(readOnly = true)
    public void checkUsername(String username) {
        if (userRepository.findByUsername(username).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 아이디입니다."
            );
        }
    }

    @Transactional(readOnly = true)
    public void checkEmail(String email) {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 등록된 이메일입니다."
            );
        }
    }

    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        if (userRepository.findByNickname(nickname).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "이미 사용 중인 닉네임입니다."
            );
        }
    }
}