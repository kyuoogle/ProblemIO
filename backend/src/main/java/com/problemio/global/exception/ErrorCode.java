package com.problemio.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다."),
    EMAIL_DUPLICATED("U002", "이미 사용 중인 이메일입니다."),
    NICKNAME_DUPLICATED("U003", "이미 사용 중인 닉네임입니다."),
    INVALID_LOGIN("U004", "이메일 또는 비밀번호가 올바르지 않습니다."),
    QUIZ_NOT_FOUND("Q001", "퀴즈를 찾을 수 없습니다."),
    QUESTION_NOT_FOUND("Q002", "문제를 찾을 수 없습니다."),
    COMMENT_NOT_FOUND("C001", "댓글을 찾을 수 없습니다."),
    ACCESS_DENIED("A001", "접근 권한이 없습니다."),
    INTERNAL_SERVER_ERROR("S001", "서버 내부 오류가 발생했습니다."),
    CANNOT_LIKE_OWN_QUIZ("Q003", "자신이 만든 퀴즈에는 좋아요를 누를 수 없습니다.");


    private final String code;
    private final String message;
}
