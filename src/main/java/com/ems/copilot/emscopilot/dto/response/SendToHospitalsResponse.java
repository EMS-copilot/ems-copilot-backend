package com.ems.copilot.emscopilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 병원 전송 결과 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendToHospitalsResponse {

    private String sessionId;
    private int totalSent; // 전송한 병원 수

    // 환자 바이탈 정보 (프론트엔드 확인용)
    private PatientVitalInfo patientVital;

    private List<HospitalRequestInfo> requests; // 생성된 요청 목록

    /**
     * 환자 바이탈 정보 (Redis에서 조회한 데이터)
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PatientVitalInfo {
        private Integer age;
        private String sex;
        private Integer triageLevel;
        private Integer sbp;
        private Integer dbp;
        private Integer hr;
        private Integer rr;
        private Integer spo2;
        private Double temp;
        private List<String> symptoms;
    }

    /**
     * 각 병원별 요청 정보
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HospitalRequestInfo {
        private Long requestId; // HospitalRequest ID
        private Long hospitalId;
        private String hospitalName;
        private String status; // PENDING
    }
}
