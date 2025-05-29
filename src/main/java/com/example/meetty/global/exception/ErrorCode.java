package com.example.meetty.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Auth 에러코드
    UNAUTHORIZED_ACCESS(HttpStatus.UNAUTHORIZED, "허용되지 않은 사용자입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 에러가 발생하였습니다."),
    USER_EMAIL_DUPLICATED(HttpStatus.CONFLICT, "이미 가입된 이메일입니다."),
    USER_EMAIL_NOT_FOUND(HttpStatus.NOT_FOUND, "이메일을 찾을 수 없습니다."),
    USERNAME_DUPLICATED(HttpStatus.CONFLICT, "이미 존재하는 닉네임입니다."),
    BINDING_RESULT_ERROR(HttpStatus.BAD_REQUEST, "데이터 유효성에 문제가 있습니다."),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "유효하지 않은 입력 값입니다."),
    CHECK_EMAIL_OR_PASSWORD(HttpStatus.NOT_FOUND, "이메일 또는 비밀번호가 올바르지 않습니다."),
    NOT_EQUAL_PASSWORD(HttpStatus.BAD_REQUEST, "새 비밀번호와 확인 비밀번호가 일치하지 않습니다."),
    VALID_ACCESS_TOKEN(HttpStatus.BAD_REQUEST, "유효한 Access Token 입니다."),
    NOT_FOUND_REFRESH_TOKEN(HttpStatus.NOT_FOUND, "존재하지 않는 Refresh Token 입니다."),
    NOT_FOUND_COOKIE(HttpStatus.NOT_FOUND, "쿠키 값이 존재하지 않습니다. 다시 로그인 해주세요."),
    INCORRECT_REFRESH_TOKEN(HttpStatus.CONFLICT, "Refresh Token 이 일치하지 않습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.BAD_REQUEST, "유효하지 않은 토큰입니다. 다시 로그인 해주세요."),
    IMAGE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 업로드에 실패하였습니다." ),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 유저를 찾을 수 없습니다."),
    INVALID_JSON_FORMAT(HttpStatus.BAD_REQUEST, "요청 데이터 형식이 올바르지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "현재 비밀번호가 일치하지 않습니다."),

    // Email 에러코드
    EMAIL_SEND_FAIL(HttpStatus.BAD_REQUEST, "이메일 전송에 실패하였습니다."),
    INVALID_EMAIL_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 이메일 인증 토큰입니다."),
    EMAIL_ALREADY_VERIFIED(HttpStatus.CONFLICT, "이미 인증한 이메일 입니다"),
    EMAIL_NOT_VERIFIED(HttpStatus.UNAUTHORIZED, "이메일 인증이 완료되지 않은 유저입니다."),

    // StudyRoom 에러코드
    STUDY_GROUP_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 스터디 그룹을 찾을 수 없습니다."),
    UNAUTHORIZED_STUDY_GROUP_ACCESS(HttpStatus.FORBIDDEN, "스터디 그룹에 대한 권한이 없습니다."),
    STUDY_GROUP_DELETE_FAILED_HAS_MEMBERS(HttpStatus.BAD_REQUEST, "스터디 그룹에 참여 중인 다른 멤버가 있어 삭제할 수 없습니다."),
    // 스터디 그룹 참가/초대 관련 에러 (새로 추가)
    ALREADY_STUDY_GROUP_MEMBER(HttpStatus.CONFLICT, "이미 스터티 그룹 멤버입니다."),
    STUDY_GROUP_CAPACITY_FULL(HttpStatus.CONFLICT, "스터디 그룹 인원이 가득 찼습니다."),
    STUDY_GROUP_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "해당 스터디 그룹 멤버를 찾을 수 없습니다."),
    STUDY_GROUP_MEMBER_MISMATCH(HttpStatus.BAD_REQUEST, "요청된 스터디 그룹과 멤버 정보가 일치하지 않습니다."),
    EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이메일 발송에 실패했습니다."),
    INVITE_TOKEN_INVALID(HttpStatus.BAD_REQUEST, "유효하지 않거나 만료된 초대 링크입니다."),
    INVITE_TOKEN_ALREADY_USED(HttpStatus.BAD_REQUEST, "이미 사용된 초대 링크입니다."),
    ALREADY_BANNED_FROM_STUDY_GROUP(HttpStatus.FORBIDDEN,"해당 스터디 그룹에서 차단되어 재가입할 수 없습니다."),
    REGION_MISMATCH(HttpStatus.FORBIDDEN,"해당 스터디 그룹에 설정된 지역와 회원님이 설정한 지역이 일치하지 않습니다."),

    // Chat 에러코드
    UNAUTHORIZED_STUDY_ROOM_CHAT(HttpStatus.FORBIDDEN, "해당 스터디방에 참여하지 않은 유저입니다."),

    // Notification 에러코드
    NOT_FOUND_NOTIFICATION(HttpStatus.NOT_FOUND, "해당 알림을 찾을 수 없습니다.")

    ;

    private final HttpStatus httpStatus;
    private final String message;
}
