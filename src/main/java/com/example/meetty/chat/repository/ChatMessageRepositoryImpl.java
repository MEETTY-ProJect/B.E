package com.example.meetty.chat.repository;

import com.example.meetty.auth.entity.QUserEntity;
import com.example.meetty.chat.dto.ChatMessageResponseDto;
import com.example.meetty.chat.dto.QChatMessageResponseDto;
import com.example.meetty.chat.entity.QChatMessage;
import com.example.meetty.image.entity.QUserImageEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class ChatMessageRepositoryImpl implements ChatMessageRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public ChatMessageRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public List<ChatMessageResponseDto> findMessagesByRoomId(Long roomId, Long lastMessageId, int limit) {

        QChatMessage m = QChatMessage.chatMessage;
        QUserEntity u = QUserEntity.userEntity;
        QUserImageEntity img = QUserImageEntity.userImageEntity;

        return queryFactory
                .select(new QChatMessageResponseDto(
                        m.messageId,
                        m.room.roomId,
                        u.userId,
                        u.username,
                        img.url.coalesce(""),
                        m.message,
                        m.createdAt))
                .from(m)
                .join(m.sender,u)
                .leftJoin(img).on(img.userEntity.eq(u))
                .where(
                        m.room.roomId.eq(roomId),
                        lastMessageId != null ? m.messageId.lt(lastMessageId) : null
                )
                .orderBy(m.createdAt.desc())
                .limit(limit)
                .fetch();
    }
}
