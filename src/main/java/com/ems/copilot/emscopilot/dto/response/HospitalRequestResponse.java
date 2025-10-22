package com.ems.copilot.emscopilot.dto.response;

import com.ems.copilot.emscopilot.domain.HospitalRequest;
import com.ems.copilot.emscopilot.domain.RequestStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 병원 요청 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class HospitalRequestResponse {

    private Long id;
    private String sessionId;

    // 병원 정보
    private Long hospitalId;
    private String hospitalName;

    // 환자 간단 정보
    private Integer age;
    private String sex;
    private Integer triageLevel;
    private List<String> symptoms;

    // 응답 정보
    private RequestStatus status;
    private String responseMessage;

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;

    /**
     * Entity -> DTO 변환
     */
    public static HospitalRequestResponse from(HospitalRequest entity) {
        // JSON 문자열을 List<String>으로 변환
        List<String> symptomsList = parseSymptomsJson(entity.getSymptoms());

        return HospitalRequestResponse.builder()
                .id(entity.getId())
                .sessionId(entity.getSessionId())
                .hospitalId(entity.getHospital().getId())
                .hospitalName(entity.getHospital().getName())
                .age(entity.getAge())
                .sex(entity.getSex())
                .triageLevel(entity.getTriageLevel())
                .symptoms(symptomsList)
                .status(entity.getStatus())
                .responseMessage(entity.getResponseMessage())
                .createdAt(entity.getCreatedAt())
                .respondedAt(entity.getRespondedAt())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    /**
     * JSON 문자열을 List<String>으로 파싱
     */
    private static List<String> parseSymptomsJson(String symptomsJson) {
        if (symptomsJson == null || symptomsJson.isEmpty()) {
            return Collections.emptyList();
        }

        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(symptomsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.error("증상 JSON 파싱 실패: {}", symptomsJson, e);
            return Collections.emptyList();
        }
    }
}
