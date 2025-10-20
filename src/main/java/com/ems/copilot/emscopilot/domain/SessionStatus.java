package com.ems.copilot.emscopilot.domain;

public enum SessionStatus {
    PENDING,      // 대기 중
    MATCHING,     // 매칭 중
    MATCHED,      // 매칭 완료
    REJECTED,     // 거절됨
    EXPIRED,      // 만료됨
    CANCELLED,    // 취소됨
    COMPLETED     // 완료됨
}
