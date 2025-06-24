package com.blc.blc_backend.auth.security;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.net.URI;
import java.util.Map;

@Slf4j
@Component
public class FirebaseWebSocketInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String token = extractTokenFromQuery(request);
        
        if (token != null) {
            try {
                FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
                attributes.put("firebaseUid", decodedToken.getUid());
                attributes.put("userEmail", decodedToken.getEmail());
                attributes.put("userName", decodedToken.getName());
                
                log.info("WebSocket connection authenticated for user: {}", decodedToken.getUid());
                return true;
                
            } catch (FirebaseAuthException e) {
                log.warn("WebSocket Firebase token verification failed: {}", e.getMessage());
                return false;
            }
        }
        
        log.warn("WebSocket connection attempted without valid token");
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 후 처리 로직
    }

    private String extractTokenFromQuery(ServerHttpRequest request) {
        URI uri = request.getURI();
        String query = uri.getQuery();
        
        if (query != null) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6); // "token=" 제거
                }
            }
        }
        
        // Authorization 헤더에서도 확인
        String authHeader = request.getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        
        return null;
    }
}