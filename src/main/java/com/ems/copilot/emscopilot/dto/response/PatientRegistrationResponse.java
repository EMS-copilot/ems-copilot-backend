package com.ems.copilot.emscopilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 환자등록시 프론트에 응답(vertex ai 응답 + db에 저장된 병원 상세 정보 포함)
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PatientRegistrationResponse {

    // 세션 정보
    private String sessionId;
    private String sessionCode; // P2025-001

    // 환자 임시 ID
    private String patientTempId;

    private List<RecommendedHospital> recommendedHospitals;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String status;

    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedHospital {

        // 병원 식별
        private String hospitalId; // "CBH_001"
        private String hospitalName;

        // AI 분석
        private Double aiScore;
        private Integer priority;
        private Map<String, Double> aiExplanations;

        // 병원 상세
        private Double distance;
        private Integer eta;
    }
}
