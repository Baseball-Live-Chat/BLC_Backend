package com.blc.blc_backend.config;

import com.blc.blc_backend.auth.security.FirebaseAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    // Firebase Token 기반으로 변경
    private final FirebaseAuthFilter firebaseAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authz -> authz
                        // Preflight OPTIONS 요청 허용
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        
                        // Firebase 토큰 기반 인증이 필요한 엔드포인트
                        .requestMatchers("/api/auth/users/profile").authenticated()
                        .requestMatchers("/api/chat/**").authenticated()
                        
                        // WebSocket 엔드포인트는 인터셉터에서 처리
                        .requestMatchers("/chat-socket/**").permitAll()
                        
                        // 기타 요청은 모두 허용 (회원가입, 동기화 등)
                        .anyRequest().permitAll()
                )
                .addFilterBefore(firebaseAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "https://localhost:5173",
                "http://13.209.49.84:8080",
                "https://blc.ai.kr",
                "https://blc-frontent.web.app",    // 구체적인 Firebase 도메인
                "https://blc-frontent.firebaseapp.com",  // 구체적인 Firebase 도메인
                "https://blcback.shop/chat-socket"
        ));
        cfg.setAllowedMethods(List.of("GET","POST","PUT","DELETE","OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource src = new UrlBasedCorsConfigurationSource();
        src.registerCorsConfiguration("/**", cfg);
        return src;
    }
}