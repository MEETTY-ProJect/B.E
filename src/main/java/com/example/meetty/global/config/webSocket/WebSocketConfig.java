package com.example.meetty.global.config.webSocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    //클라이언트가 WebSocket 서버에 연결할 때 사용할 엔드포인트 설정
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws") // 클라이언트 연결 주소
                .setAllowedOriginPatterns("*") // CORS 허용 (*는 개발용)
                .addInterceptors(jwtHandshakeInterceptor) // 인증용 인터셉터 추가
                .withSockJS(); // SockJS: 브라우저 호환성 위한 fallback
    }

    // STOMP 메시지 라우팅 설정
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 경로(prefix)
        registry.enableSimpleBroker("/topic");

        // 클라이언트가 서버로 보낼 때 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }
}
