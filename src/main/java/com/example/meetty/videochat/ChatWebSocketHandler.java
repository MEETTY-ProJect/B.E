package com.example.meetty.videochat;

import com.example.meetty.board.repository.StudyMembersRepository;
import com.example.meetty.chat.dto.ChatMessageRequestDto;
import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.service.ChatMessageService;
import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final ChatMessageService chatMessageService;
    private final JwtTokenProvider jwtTokenProvider;
    private final StudyMembersRepository studyMembersRepository;

    //채팅방별 세션 관리 (브로드캐스팅용)
    private final Map<Long, Set<WebSocketSession>> chatRooms = new ConcurrentHashMap<>();
    // ObjectMapper에 JavaTimeModule 등록 (LocalDateTime 지원)
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("💡 WebSocket 연결 시도됨");

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

        //1.클라이언트에서 들어온 메시지를 파싱
        ChatMessageRequestDto requestDto = objectMapper.readValue(message.getPayload(), ChatMessageRequestDto.class);

        //2.DB에 메시지 저장 + 응답 DTO 생성
        ChatMessageResponseDto savedMessage = chatMessageService.saveMessage(roomId,userId,requestDto);

        //3. WebSocket으로 모든 세션에 브로드 캐스트
        String sendMessage = objectMapper.writeValueAsString(savedMessage);

        for (WebSocketSession s : chatRooms.getOrDefault(roomId,Set.of())) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(sendMessage));
            }
        }

        log.info("[채팅 전송] userId={}, roomId={}, message= {}", userId, roomId, savedMessage.getMessage());

    }






    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = (Long) session.getAttributes().get("roomId");
        if (roomId != null) {
            chatRooms.getOrDefault(roomId, Set.of()).remove(session);
            log.info("[채팅 종료] roomId={}, reason={}", roomId, status.getReason());
        }
    }


    //쿼리 파라미터 토큰 파싱
    private String getTokenFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        Map<String, String> params = parseQueryParams(uri.getQuery());
        String token = params.get("token");

        if (token == null || token.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return token;
    }

    private Long getRoomIdFromSession(WebSocketSession session) {
        URI uri = session.getUri();
        Map<String, String> params = parseQueryParams(uri.getQuery());
        String roomId = params.get("room");
        if (roomId == null || roomId.isEmpty()) {
            throw new AppException(ErrorCode.STUDY_GROUP_NOT_FOUND);
        }
        return Long.parseLong(roomId);
    }


    private Map<String, String> parseQueryParams(String query) {
        if (query == null || query.isEmpty()) {
            return Collections.emptyMap();
        }
        return Arrays.stream(query.split("&"))
                .map(s -> s.split("=",2))
                .collect(Collectors.toMap(
                        arr -> arr[0],
                        arr -> arr.length > 1 ? arr[1] : ""
                ));

    }

    private void broadcastMessage(Long roomId,ChatMessageRequestDto message) {
        Set<WebSocketSession> sessions = chatRooms.get(roomId);
        if (sessions == null) return;

        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("메시지 변환 실패", e);
            return;
        }
        for (WebSocketSession s : sessions) {
            if (s.isOpen()) {
                try {
                    s.sendMessage(new TextMessage(jsonMessage));
                } catch (Exception e) {
                    log.error("메시지 전송 실패", e);
                }
            }
        }
    }




}
