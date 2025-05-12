package com.example.meetty.board.service;

import com.example.meetty.auth.entity.UserEntity;
import com.example.meetty.auth.repository.UserRepository;
import com.example.meetty.board.entity.JoinRequestEntity;
import com.example.meetty.board.entity.StudyGroupEntity;
import com.example.meetty.board.entity.StudyMembersEntity;
import com.example.meetty.board.repository.JoinRequestRepository;
import com.example.meetty.board.repository.StudyGroupRepository;
import com.example.meetty.board.repository.StudyMembersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardService {
    private final StudyGroupRepository studyGroupRepository;
    private final StudyMembersRepository studyGroupMemberRepository;
    private final UserRepository userRepository;
    @Value("${app.invitation-code-expiry-minutes:60}") // 기본값 60분
    private long invitationExpiryMinutes;

    private StudyGroupEntity createStudyGroupEntity(String groupName, String reason, int capacity, String purpose, String region, UserEntity hostUser) {
        return StudyGroupEntity.builder()
                .groupName(groupName)
                .reason(reason)
                .capacity(capacity)
                .purpose(purpose)
                .region(region)
                .createdAt(LocalDateTime.now())
                .hostUserId(hostUser)
                .build();
    }
    private StudyMembersEntity createStudyMembersEntity(StudyGroupEntity group, UserEntity memberUser, StudyMembersEntity.MemberStatus status) {
        return StudyMembersEntity.builder()
                .studyGroup(group)
                .member(memberUser)
                .joinedAt(LocalDateTime.now())
                .status(status)
                .build();
    }

    private JoinRequestEntity createJoinRequestEntity(StudyGroupEntity studyGroup, UserEntity guestUser, String invitationCode, LocalDateTime expiresAt) {
        return JoinRequestEntity.builder()
                .studyGroup(studyGroup)
                .user(guestUser)
                .token(invitationCode)
                .expiresAt(expiresAt)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public StudyGroupEntity createGroup(Long hostUserId, String groupName, String reason, int capacity, String purpose, String region) {
        UserEntity hostUser = userRepository.findById(hostUserId)
                .orElseThrow(() -> new IllegalArgumentException("Host user not found with id: " + hostUserId));

        StudyGroupEntity studyGroup = createStudyGroupEntity(
                groupName, reason, capacity, purpose, region, hostUser
        );

        StudyGroupEntity savedGroup = studyGroupRepository.save(studyGroup);

        addMemberToGroup(savedGroup, hostUser, StudyMembersEntity.MemberStatus.ACTIVE);

        return savedGroup;
    }

    @Transactional
    public StudyMembersEntity addMemberToGroup(StudyGroupEntity group, UserEntity user, StudyMembersEntity.MemberStatus status) {
        if (studyGroupMemberRepository.existsByStudyGroupAndMember(group, user)) {
            throw new IllegalStateException("User is already a member of this group.");
        }

        StudyMembersEntity member = createStudyMembersEntity(group, user, status);

        return studyGroupMemberRepository.save(member);
    }


}

//    @Value("${spring.mail.username}")
//    private String fromEmail;
//    @Value("${app.base-url:http://localhost:8080}")
//    private String baseUrl;
//    @Value("${app.join-confirm-path:/api/groups/join/confirm-link}")
//    private String joinConfirmPath;


//@Transactional
//    public void requestJoinGroup(Long groupId, Long guestUserId) {
//        StudyGroupEntity studyGroup = studyGroupRepository.findById(groupId)
//                .orElseThrow(() -> new IllegalArgumentException("Study group not found with id: " + groupId));
//
//        UserEntity guestUser = userRepository.findById(guestUserId)
//                .orElseThrow(() -> new IllegalArgumentException("Guest user not found with id: " + guestUserId));
//
//        if (studyGroup.getHostUserId().getUserId().equals(guestUserId)) {
//            throw new IllegalStateException("Host cannot join their own group as a guest.");
//        }
//
//        if (studyGroupMemberRepository.existsByStudyGroupAndMember(studyGroup, guestUser)) {
//            throw new IllegalStateException("User is already an active member of this group.");
//        }
//
//        String invitationToken = UUID.randomUUID().toString();
//
//        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(invitationCodeExpiryMinutes);
//        JoinRequestEntity joinRequest = createJoinRequestEntity(
//                studyGroup, guestUser, invitationToken, expiresAt
//        );
//
//        joinRequestRepository.save(joinRequest);
//
//        String invitationLink = UriComponentsBuilder.fromHttpUrl(baseUrl)
//                .path(joinConfirmPath)
//                .queryParam("token", invitationToken)
//                .build().toUriString();
//
//        sendInvitationEmailWithLink(studyGroup, guestUser, invitationLink);
//    }
//
//    @Transactional
//    public StudyMembersEntity confirmJoinGroup(Long groupId, Long guestUserId, String token) {
//        JoinRequestEntity joinRequest = joinRequestRepository.findByToken(token)
//                .orElseThrow(() -> new IllegalArgumentException("Invalid or expired invitation link."));
//
//        if (joinRequest.getExpiresAt().isBefore(LocalDateTime.now())) {
//            try { joinRequestRepository.delete(joinRequest); } catch (Exception e){
//            throw new IllegalArgumentException("Invitation link has expired.");
//        }}
//
//        StudyGroupEntity studyGroup = joinRequest.getStudyGroup();
//        UserEntity guestUser = joinRequest.getUser();
//
//        if (studyGroupMemberRepository.existsByStudyGroupAndMember(studyGroup, guestUser)) {
//            try { joinRequestRepository.delete(joinRequest); } catch (Exception e) {
//            throw new IllegalStateException("User is already an active member of this group.");
//        }}
//
//        StudyMembersEntity member = addMemberToGroup(studyGroup, guestUser, StudyMembersEntity.MemberStatus.ACTIVE);
//
//        try { joinRequestRepository.delete(joinRequest); } catch (Exception e) {throw new IllegalArgumentException("에러 설명"); }
//
//        return member;
//        }
//
//private void sendInvitationEmailWithLink(StudyGroupEntity group, UserEntity guestUser, String invitationLink) {
//        SimpleMailMessage message = new SimpleMailMessage();
//        message.setFrom(fromEmail);
//        message.setTo(guestUser.getEmail());
//        message.setSubject("[" + group.getGroupName() + "] 스터디 그룹 참가 초대 링크입니다.");
//
//        String emailContent = String.format(
//        "안녕하세요, %s님!\n\n" +
//        "스터디 그룹 [%s]에 초대되셨습니다.\n" +
//        "아래 링크를 클릭하여 그룹에 참가해주세요.\n\n" +
//        "초대 링크: %s\n\n" +
//        "이 링크는 %d분 후 만료됩니다.\n" +
//        "감사합니다.\n",
//        guestUser.getUsername(),
//        group.getGroupName(),
//        invitationLink,
//        invitationCodeExpiryMinutes
//        );
//        message.setText(emailContent);
//
//        try {
//        mailSender.send(message);
//        System.out.println("Invitation email with link sent to " + guestUser.getEmail() + " for group " + group.getGroupName());
//        } catch (Exception e) {
//        System.err.println("Failed to send invitation email with link: " + e.getMessage());
//        }
//
//        }
