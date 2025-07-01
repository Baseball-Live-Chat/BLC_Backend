package com.blc.blc_backend.user.controller;

import com.blc.blc_backend.user.dto.UserRequestDto;
import com.blc.blc_backend.user.dto.UserResponseDto;
import com.blc.blc_backend.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /** 회원가입 */
    @PostMapping
    public ResponseEntity<UserResponseDto> createUser(@RequestBody UserRequestDto request) {
        UserResponseDto response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /** 전체 유저 조회 */
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        List<UserResponseDto> list = userService.getAllUsers();
        return ResponseEntity.ok(list);
    }

    /** 단건 유저 조회 */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUser(@PathVariable("id") Long id) {
        UserResponseDto dto = userService.getUserById(id);
        return ResponseEntity.ok(dto);
    }

    /** 유저 정보 수정 */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateUser(
            @PathVariable("id") Long id,
            @RequestBody UserRequestDto request
    ) {
        UserResponseDto dto = userService.updateUser(id, request);
        return ResponseEntity.ok(dto);
    }

    /** 유저 삭제 */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    // ————————————————————————————————————————————
    // 프론트 비동기 중복체크용 전용 엔드포인트 (onBlur 또는 “중복 확인” 버튼에 사용)

    /** 아이디(로그인명) 중복 확인 */
    @GetMapping("/check/username")
    public ResponseEntity<Void> checkUsername(@RequestParam String username) {
        userService.checkUsername(username);
        return ResponseEntity.ok().build();
    }

    /** 이메일 중복 확인 */
    @GetMapping("/check/email")
    public ResponseEntity<Void> checkEmail(@RequestParam String email) {
        userService.checkEmail(email);
        return ResponseEntity.ok().build();
    }

    /** 닉네임 중복 확인 */
    @GetMapping("/check/nickname")
    public ResponseEntity<Void> checkNickname(@RequestParam String nickname) {
        userService.checkNickname(nickname);
        return ResponseEntity.ok().build();
    }
}
