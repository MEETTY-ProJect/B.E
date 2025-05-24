package com.example.meetty.global.config.webSocket.notification;

import com.example.meetty.global.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
        // Query string 또는 Header에서 토큰 추출
        String token = getTokenFromRequest(request);
        log.info("🔐 [Handshake] 들어온 토큰: {}", token);

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // 토큰에서 userId 추출
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            log.info("✅ [Handshake] 인증 성공 - userId: {}", userId);

            // 인증된 사용자 정보 저장
            attributes.put("userId", userId);
            return true;
        }

        log.warn("❌ [Handshake] 인증 실패 - 연결 거부됨");
        return false;
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
    }

    private String getTokenFromRequest(ServerHttpRequest request) {
        // 예: ws://localhost:8080/ws?token=JWT
        URI uri = request.getURI();
        String query = uri.getQuery(); // token=...

        if (query != null && query.startsWith("token=")) {
            return query.substring("token=".length());
        }

        // 헤더로도 받을 수 있도록 확장 가능
        return null;
    }
}
