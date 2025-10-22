package com.ems.copilot.emscopilot.domain;

/**
 * 병원 수용 요청 상태
 */
public enum RequestStatus {
    PENDING,    // 대기 중 (병원 응답 대기)
    ACCEPTED,   // 수용
    REJECTED,   // 거절
    EXPIRED     // 만료 (30분 경과)
}
