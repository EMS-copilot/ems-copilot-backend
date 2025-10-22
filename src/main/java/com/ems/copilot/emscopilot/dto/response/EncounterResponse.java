package com.ems.copilot.emscopilot.dto.response;

import com.ems.copilot.emscopilot.domain.Encounter;
import com.ems.copilot.emscopilot.domain.EncounterStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Encounter 응답 DTO
 *
 * 개인정보 보호: 바이탈 정보는 포함하지 않음
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EncounterResponse {

    private Long id;

    // 환자 정보
    private String patientCode;
    private String patientTempId;
    private String sessionCode;

    // 병원 정보
    private Long hospitalId;
    private String hospitalName;

    // 환자 기본 정보 (최소한만)
    private Integer age;
    private String sex;
    private Integer triageLevel;

    // 바이탈 정보 (확정 시에만 포함, 병원에 전달)
    private VitalInfo vitalInfo;

    // 이송 정보
    private String transferLocation;
    private Double transferDistance;
    private Integer transferEta;

    // AI 정보
    private Double aiScore;
    private Integer aiPriority;

    // 상태
    private EncounterStatus status;

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime transferredAt;
    private LocalDateTime completedAt;

    private String notes;

    /**
     * 바이탈 정보 내부 클래스
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VitalInfo {
        private Integer sbp;
        private Integer dbp;
        private Integer hr;
        private Integer rr;
        private Integer spo2;
        private Double temp;
        private java.util.List<String> symptoms;
    }

    /**
     * Entity -> DTO 변환 (바이탈 정보 없이)
     */
    public static EncounterResponse from(Encounter entity) {
        return EncounterResponse.builder()
                .id(entity.getId())
                .patientCode(entity.getPatientCode())
                .patientTempId(entity.getPatientTempId())
                .sessionCode(entity.getSessionCode())
                .hospitalId(entity.getHospital().getId())
                .hospitalName(entity.getHospital().getName())
                .age(entity.getAge())
                .sex(entity.getSex())
                .triageLevel(entity.getTriageLevel())
                .vitalInfo(null) // 바이탈 정보 없이 변환
                .transferLocation(entity.getTransferLocation())
                .transferDistance(entity.getTransferDistance())
                .transferEta(entity.getTransferEta())
                .aiScore(entity.getAiScore())
                .aiPriority(entity.getAiPriority())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .transferredAt(entity.getTransferredAt())
                .completedAt(entity.getCompletedAt())
                .notes(entity.getNotes())
                .build();
    }

    /**
     * Entity -> DTO 변환 (바이탈 정보 포함)
     */
    public static EncounterResponse fromWithVital(Encounter entity, VitalInfo vitalInfo) {
        EncounterResponse response = from(entity);
        response.setVitalInfo(vitalInfo);
        return response;
    }
}
