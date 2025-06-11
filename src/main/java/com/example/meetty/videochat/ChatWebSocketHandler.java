package com.example.meetty.videochat;

import com.example.meetty.board.repository.StudyMembersRepository;
import com.example.meetty.chat.dto.ChatMessageRequestDto;
import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.service.ChatMessageService;
import com.example.meetty.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StudyMembersRepository studyMembersRepository;


    private final Map<Long, Set<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        URI uri = session.getUri();
        if (uri == null || uri.getQuery() == null) {
            session.close(CloseStatus.BAD_DATA.withReason("요청 파라미터 없음"));
            return;
        }

        Map<String,String> params = parseQueryParams(uri.getQuery());

        Long roomId = Long.parseLong(params.get("room"));

        String token = params.get("token");

        if (!jwtTokenProvider.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("jwt 인증 실패"));
            return;
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);

        if (!studyMembersRepository.existsByStudyRoom_RoomIdAndMember_UserId(roomId, userId)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("스터디방 멤버 아님"));
            return;
        }

        session.getAttributes().put("userId", userId);
        session.getAttributes().put("roomId", roomId);
        chatRooms.computeIfAbsent(roomId, r -> ConcurrentHashMap.newKeySet()).add(session);

        log.info("[채팅 연결] userId={}, roomId={}", userId, roomId);


    }


    //실시간 메시지 전송
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        Long userId = (Long) session.getAttributes().get("userId");

        ChatMessageRequestDto requestDto = objectMapper.readValue(message.getPayload(), ChatMessageRequestDto.class);
        ChatMessageResponseDto responseDto = chatMessageService.saveMessage(roomId,userId,requestDto);

        String responseJson = objectMapper.writeValueAsString(responseDto);
        for (WebSocketSession s : chatRooms.getOrDefault(roomId,Set.of())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(responseJson));
            }
        }

        log.info("[채팅 전송] userId={}, roomId={}, message= {}", userId, roomId, responseDto.getMessage());

    }






    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        if (roomId != null) {
            chatRooms.getOrDefault(roomId, Set.of()).remove(session);
            log.info("[채팅 종료] roomId={}, reason={}", roomId, status.getReason());
        }
    }






    private Map<String, String> parseQueryParams(String query) {

        Map<String, String> result = new HashMap<>();
        if (query == null || query.isBlank())
            return result;

        for (String param : query.split("&")) {
            String[] kv = param.split("=", 2);
            if (kv.length == 2 && !kv[0].isBlank() && !kv[1].isBlank()) {
                result.put(kv[0], kv[1]);
            }
        }
        return result;
    }




}
