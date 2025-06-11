package com.example.meetty.global.config.webSocket.videochat;

import com.example.meetty.videochat.MeshSignalingHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfigForMesh implements WebSocketConfigurer {

    private final MeshSignalingHandler meshSignalingHandler;

    //실시간 시그널링 설정-mesh용
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(meshSignalingHandler, "/videochat1")
                .setAllowedOrigins("*");
    }


}
