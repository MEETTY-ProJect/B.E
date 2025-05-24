package com.example.meetty.global.config.webSocket.videochat;

import com.example.meetty.videochat.SignalingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfigForSignaling implements WebSocketConfigurer {

    private final SignalingHandler signalingHandler;

    //실시간 시그널링 설정
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(signalingHandler, "/ws/video")
                .setAllowedOrigins("*");
    }


}
