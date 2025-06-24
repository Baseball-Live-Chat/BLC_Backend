package com.blc.blc_backend.config;

import com.blc.blc_backend.auth.security.FirebaseWebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final FirebaseWebSocketInterceptor firebaseWebSocketInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic"); // 구독 접두사
        config.setApplicationDestinationPrefixes("/app"); // 메시지 전송 접두사
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록 (Firebase 인증 포함)
        registry.addEndpoint("/chat-socket")
                .setAllowedOriginPatterns("*") // 개발 환경에서는 모든 도메인 허용
                .addInterceptors(firebaseWebSocketInterceptor) // Firebase 인증 인터셉터 추가
                .withSockJS(); // SockJS 지원 추가
    }
}