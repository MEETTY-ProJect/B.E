package com.example.meetty.videochat;

import com.example.meetty.board.repository.StudyMembersRepository;
import com.example.meetty.global.jwt.JwtTokenProvider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class MeshSignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final JwtTokenProvider jwtTokenProvider;
    private final StudyMembersRepository studyMembersRepository;

    // 세션 → 유저 ID
    private final Map<WebSocketSession, Long> sessionUserId = new ConcurrentHashMap<>();
    // 세션 → 방 ID
    private final Map<WebSocketSession, Long> sessionRoomId = new ConcurrentHashMap<>();
    // 유저 ID → 세션
    private final Map<Long, WebSocketSession> userIdSession = new ConcurrentHashMap<>();
    // 방 ID → 해당 방의 모든 세션들
    private final Map<Long, Set<WebSocketSession>> roomSessions = new ConcurrentHashMap<>();


    //WebSocket 연결시 실행
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        // 쿼리스트링에서 roomId, token 추출
        String query = session.getUri().getQuery(); // ex) room=123&token=xxx
        Long roomId = null;
        String token = null;

        if (query != null) {
            for (String param : query.split("&")) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    if ("room".equals(kv[0])) roomId = Long.parseLong(kv[1]) ;
                    if ("token".equals(kv[0])) token = kv[1];
                }
            }
        }

        // 필수값 체크
        if (roomId == null || token == null) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("roomId/token 누락"));
            return;
        }

        // JWT 토큰 검증
        if (!jwtTokenProvider.validateToken(token)) {
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("JWT 인증 실패"));
            return;
        }
        //userId 추출
        Long userId = jwtTokenProvider.getUserIdFromToken(token);



        //해당 방의 멤버가 아니라면 연결 차단
        List<Long> memberIds = getMemberIds(roomId);
        if (!memberIds.contains(userId)) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"해당 방의 멤버가 아닙니다.\"}"));
            session.close(CloseStatus.NOT_ACCEPTABLE.withReason("멤버 아님"));
            return;
        }

        //연결된 세션, 유저, 방 정보 등록
        sessionUserId.put(session, userId);
        sessionRoomId.put(session, roomId);
        userIdSession.put(userId, session);
        roomSessions.putIfAbsent(roomId, ConcurrentHashMap.newKeySet());
        roomSessions.get(roomId).add(session);

    }

    //WebSocket 연결 종료시 실행
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Long roomId = sessionRoomId.remove(session);
        Long userId = sessionUserId.remove(session);
        if (roomId != null && userId != null) {
            Set<WebSocketSession> sessions = roomSessions.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
            }
            userIdSession.remove(userId);
        }
    }

    //시그널링 메시지 수신시 실행
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        JsonNode json = objectMapper.readTree(message.getPayload());
        String type = json.has("type") ? json.get("type").asText() : null;
        Long roomId = sessionRoomId.get(session);
        Long userId = sessionUserId.get(session);

        if (type ==null || roomId == null || userId == null) {
            session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"인증/방 정보 누락\"}"));
            session.close();
            return;
        }

        List<Long> memberIds = getMemberIds(roomId);

        switch (type) {

            case "join" -> {

                if (!memberIds.contains(userId)) {
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"해당 방의 멤버가 아닙니다.\"}"));
                    session.close();
                    return;
                }

                List<Long> otherUserIds = new ArrayList<>(memberIds);
                otherUserIds.remove(userId);// 본인은 제외

                Map<String, Object> participantsMsg = new HashMap<>();
                participantsMsg.put("type", "participants");
                participantsMsg.put("userIds", otherUserIds);

                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(participantsMsg)));
            }

            //시그널링 메시지 (offer, answer, candidate)
            case "offer", "answer", "candidate" -> {
                Long to = json.has("to") ? json.get("to").asLong() : null;

                if (to == null || !memberIds.contains(userId) || !memberIds.contains(to)){
                    session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"유효하지 않은 시그널링입니다.\"}"));
                    return;
                }

                WebSocketSession toSession = userIdSession.get(to);
                if (toSession == null && toSession.isOpen()) {
                    toSession.sendMessage(message);
                }
            }

            default -> session.sendMessage(new TextMessage("{\"type\":\"error\",\"message\":\"지원하지 않는 메시지 유형\"}"));

        }
    }


    //방 ID로 멤버 userID 리스트 반환하는 공통 메서드
    private List<Long> getMemberIds(Long roomId) {
        return studyMembersRepository.findByStudyRoom_RoomId(roomId)
                .stream()
                .filter(m-> m.getMember() !=null)
                .map(m -> m.getMember().getUserId())
                .toList();

    }

































}
