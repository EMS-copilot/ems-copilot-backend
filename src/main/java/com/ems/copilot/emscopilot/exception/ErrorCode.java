package com.ems.copilot.emscopilot.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "U002", "비밀번호가 일치하지 않습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN, "U003", "접근 권한이 없습니다."),
    USER_HAS_NO_HOSPITAL(HttpStatus.BAD_REQUEST, "U004", "병원 정보가 없는 사용자입니다."),

    // 환자 및 세션 관련
    SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "P001", "세션을 찾을 수 없습니다."),
    SESSION_EXPIRED(HttpStatus.GONE, "P002", "세션이 만료되었습니다."),
    VITAL_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "P003", "바이탈 정보를 찾을 수 없습니다. 만료되었거나 존재하지 않습니다."),
    PATIENT_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "P004", "환자 코드를 찾을 수 없습니다."),

    // 병원 관련
    HOSPITAL_NOT_FOUND(HttpStatus.NOT_FOUND, "H001", "병원을 찾을 수 없습니다."),
    HOSPITAL_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "H002", "병원 요청을 찾을 수 없습니다."),
    HOSPITAL_REQUEST_EXPIRED(HttpStatus.GONE, "H003", "요청이 만료되었습니다. (30분 경과)"),
    ALREADY_RESPONDED(HttpStatus.CONFLICT, "H004", "이미 응답한 요청입니다."),
    INVALID_REQUEST_STATUS(HttpStatus.BAD_REQUEST, "H005", "수용된 병원만 확정할 수 있습니다."),
    NO_PENDING_REQUEST(HttpStatus.NOT_FOUND, "H006", "응답 가능한 병원 요청을 찾을 수 없습니다."),

    // Encounter 관련
    ENCOUNTER_NOT_FOUND(HttpStatus.NOT_FOUND, "E001", "Encounter를 찾을 수 없습니다."),
    ENCOUNTER_ALREADY_COMPLETED(HttpStatus.CONFLICT, "E002", "이미 완료된 Encounter입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}
