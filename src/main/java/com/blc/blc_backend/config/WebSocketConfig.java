package com.blc.blc_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 메시지 브로커 설정
        config.enableSimpleBroker("/topic"); // 구독 접두사
        config.setApplicationDestinationPrefixes("/app"); // 메시지 전송 접두사
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 기존 채팅용 엔드포인트
        registry.addEndpoint("/chat-socket")
                .setAllowedOriginPatterns("*")
                .withSockJS();

        // 베팅용 엔드포인트 추가 (같은 설정 사용)
        registry.addEndpoint("/betting-socket")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}
