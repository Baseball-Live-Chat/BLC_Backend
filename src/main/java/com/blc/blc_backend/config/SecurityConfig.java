package com.blc.blc_backend.config;

import com.blc.blc_backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.SessionManagementConfigurer;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserRepository userRepository;

    // 1) UserDetailsService, PasswordEncoder를 이용한 AuthenticationProvider 빈 등록
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService());
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    // 2) AuthenticationManager 빈은 AuthenticationConfiguration에서 꺼내 쓰기
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // 3) SecurityFilterChain에 AuthenticationProvider 연결
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(AbstractHttpConfigurer::disable)
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(authz -> authz
                        // 1) Preflight는 무조건 열어두기
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // 2) 내 정보 조회는 인증된 사용자만
                        .requestMatchers("/api/auth/me").authenticated()

                        // 3) 나머지 요청은(로그인, 회원가입 등) 모두 허용
                        .anyRequest().permitAll()
                )
                .sessionManagement(session -> session
                        // 로그인 시 세션 고정 공격 방지: 새 세션 ID 발급
                        .sessionFixation(SessionManagementConfigurer.SessionFixationConfigurer::migrateSession)
                        // 동시 세션 제어: 최대 1개 세션
                        .sessionConcurrency(concurrency -> concurrency
                                .maximumSessions(1)
                        )
                )
                .formLogin(AbstractHttpConfigurer::disable)    // 기본 로그인 폼 비활성화
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .logoutSuccessHandler((HttpServletRequest req, HttpServletResponse res, Authentication auth) -> {
                            // 1) HTTP 상태 200
                            res.setStatus(HttpServletResponse.SC_OK);

                            // 2) JSESSIONID 만료용 쿠키 (로그인 쿠키와 동일한 속성으로)
                            ResponseCookie deleteCookie = ResponseCookie.from("JSESSIONID", "")
                                    .domain("blcback.shop")      // ← Application 탭에 찍힌 도메인
                                    .path("/")                // ← 로그인 때 Path
                                    .maxAge(0)                // ← 즉시 만료
                                    .sameSite("None")         // ← 크로스사이트 XHR 허용
                                    .secure(true)             // ← HTTPS 전용
                                    .httpOnly(true)           // ← 로그인 때 HttpOnly 여부와 동일하게
                                    .build();

                            // Set-Cookie 헤더로 내리기
                            res.setHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
                        })
                )
        ;

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByUsername(username)
                .map(user -> org.springframework.security.core.userdetails.User.builder()
                        .username(user.getUsername())
                        .password(user.getPasswordHash())
                        .roles("USER")
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
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

