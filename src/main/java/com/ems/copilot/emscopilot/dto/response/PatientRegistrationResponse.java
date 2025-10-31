package com.ems.copilot.emscopilot.dto.response;

import lombok.*;

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
    private String sessionCode; // S2025-001

    // 환자 식별 정보
    private String patientCode;    // PT2025-001 (사람이 읽기 쉬운 코드)
    private String patientTempId;  // patient_xxx... (시스템 내부 UUID)

    private List<RecommendedHospital> recommendedHospitals;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    private String status;

    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedHospital {

        // 병원 식별
        private Long hospitalId; // DB의 병원 ID (Long)
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
