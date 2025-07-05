package com.blc.blc_backend.user.repository;

import com.blc.blc_backend.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // 아이디(로그인 ID)로 유저 찾기
    Optional<User> findByUsername(String username);

    // 이메일 중복 확인용
    Optional<User> findByEmail(String email);

    // 닉네임 중복 확인용
    Optional<User> findByNickname(String nickname);

}
