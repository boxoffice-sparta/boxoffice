package com.boxoffice.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CommonErrorCode implements ErrorCode {

    // 공통
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON-001", "요청 파라미터 형식 오류"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-002", "서버 내부 오류"),

    // 인증/인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON-003", "인증 실패"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON-004", "권한 없음"),

    // 서비스 간 통신
    FEIGN_CLIENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON-005", "서비스 간 호출 실패");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}