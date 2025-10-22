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

    // 바이탈 정보는 개인정보 보호를 위해 포함하지 않음
    // Redis에서만 임시 사용, 병원 완료 시 삭제됨

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
     * Entity -> DTO 변환
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
                // 바이탈 정보 제외 (개인정보 보호)
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
}
