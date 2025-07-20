package com.example.meetty.board.repository;

import com.example.meetty.auth.entity.QUserEntity;
import com.example.meetty.board.dto.QStudyRoomMemberListResponseDto;
import com.example.meetty.board.dto.StudyRoomMemberListResponseDto;
import com.example.meetty.board.entity.QStudyMembersEntity;
import com.example.meetty.image.entity.QUserImageEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;

import java.util.List;

public class StudyMembersRepositoryImpl implements StudyMembersRepositoryCustom{

    private final JPAQueryFactory queryFactory;

    public StudyMembersRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public List<StudyRoomMemberListResponseDto> findByStudyRoomMember_RoomId(Long roomId) {

        QStudyMembersEntity studyMembers = QStudyMembersEntity.studyMembersEntity;
        QUserEntity user = QUserEntity.userEntity;
        QUserImageEntity image = QUserImageEntity.userImageEntity;


        return queryFactory
                .select(new QStudyRoomMemberListResponseDto(
                        user.userId,
                        user.username,
                        image.url
                ))
                .from(studyMembers)
                .join(studyMembers.member,user)
                .leftJoin(image).on(image.userEntity.eq(user))
                .where(studyMembers.studyRoom.roomId.eq(roomId))
                .fetch();
    }
}
