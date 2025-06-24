package com.blc.blc_backend.config;

import com.blc.blc_backend.auth.token.FirebaseAuthenticationToken;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.List;
import java.util.Map;

/**
 * WebSocket 연결 시 Firebase 토큰을 검증하는 인터셉터
 */
@Component
@RequiredArgsConstructor
@Log4j2
public class FirebaseWebSocketInterceptor implements HandshakeInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    private final FirebaseAuth firebaseAuth;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                 WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        try {
            String token = getTokenFromRequest(request);
            
            if (StringUtils.hasText(token)) {
                FirebaseToken firebaseToken = firebaseAuth.verifyIdToken(token);
                
                // 사용자 정보를 WebSocket 세션 속성에 저장
                attributes.put("firebaseUid", firebaseToken.getUid());
                attributes.put("userEmail", firebaseToken.getEmail());
                attributes.put("userName", firebaseToken.getName());
                
                // Spring Security 컨텍스트에도 인증 정보 설정
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                FirebaseAuthenticationToken authToken = new FirebaseAuthenticationToken(
                        firebaseToken.getUid(), 
                        firebaseToken, 
                        authorities
                );
                SecurityContextHolder.getContext().setAuthentication(authToken);
                
                log.debug("WebSocket handshake authenticated for user: {} ({})", 
                         firebaseToken.getName(), firebaseToken.getUid());
                
                return true;
            } else {
                log.warn("WebSocket handshake failed: No Firebase token provided");
                return false;
            }
        } catch (FirebaseAuthException e) {
            log.error("WebSocket handshake failed: Invalid Firebase token", e);
            return false;
        } catch (Exception e) {
            log.error("WebSocket handshake failed: Unexpected error", e);
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                             WebSocketHandler wsHandler, Exception exception) {
        if (exception != null) {
            log.error("WebSocket handshake completed with error", exception);
        } else {
            log.debug("WebSocket handshake completed successfully");
        }
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        // Authorization 헤더에서 토큰 추출
        List<String> authHeaders = request.getHeaders().get("Authorization");
        if (authHeaders != null && !authHeaders.isEmpty()) {
            String bearerToken = authHeaders.get(0);
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
                return bearerToken.substring(BEARER_PREFIX.length());
            }
        }

        // 쿼리 파라미터에서 토큰 추출 (fallback)
        String query = request.getURI().getQuery();
        if (StringUtils.hasText(query)) {
            String[] params = query.split("&");
            for (String param : params) {
                if (param.startsWith("token=")) {
                    return param.substring(6); // "token=".length()
                }
            }
        }

        return null;
    }
}