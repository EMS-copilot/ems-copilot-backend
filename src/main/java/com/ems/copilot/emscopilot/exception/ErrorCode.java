package com.ems.copilot.emscopilot.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U002", "비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "U003", "접근 권한이 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
