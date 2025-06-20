// src/main/java/com/blc/blc_backend/config/SecurityConfig.java
package com.blc.blc_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (개발용)
                .csrf(csrf -> csrf.disable())

                // CORS 설정
                .cors(cors -> cors.and())

                // 모든 요청 허용 (개발용)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/**").permitAll()  // API 요청 허용
                        .requestMatchers("/ws/**").permitAll()   // WebSocket 허용
                        .requestMatchers("/h2-console/**").permitAll() // H2 콘솔 허용 (있다면)
                        .anyRequest().permitAll()  // 나머지 모든 요청 허용
                )

                // HTTP Basic 인증 비활성화
                .httpBasic(httpBasic -> httpBasic.disable())

                // Form 로그인 비활성화
                .formLogin(formLogin -> formLogin.disable())

                // 로그아웃 비활성화
                .logout(logout -> logout.disable());

        return http.build();
    }
}