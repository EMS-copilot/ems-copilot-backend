package com.ems.copilot.emscopilot.domain;

/**
 * 통합 상태 (TransferSession과 Encounter 모두 사용)
 */
public enum SessionStatus {
    PENDING,      // 대기 중 (환자 등록, 병원 찾는 중)
    IN_TRANSIT,   // 이송 중 (병원 확정, 이송 시작)
    ARRIVED,      // 이송 완료 (병원 도착)
    COMPLETED,    // 처치 종료 및 기록 완료
    CANCELLED     // 취소됨
}
