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
    private String sessionCode;
    private String patientCode;

    // 병원 정보
    private Long hospitalId;
    private String hospitalName;
    private String hospitalAddress;
    private Double distance;
    private Integer eta;

    // 환자 간단 정보
    private Integer age;
    private String sex;
    private Integer triageLevel;
    private List<String> symptoms;

    // 환자 전체 바이탈 정보 (Redis에서 조회)
    private Integer sbp;        // 수축기 혈압
    private Integer dbp;        // 이완기 혈압
    private Integer hr;         // 심박수
    private Integer rr;         // 호흡수
    private Integer spo2;       // 산소포화도
    private Double temp;        // 체온
    private String paramedicMemo;  // 구급대원 인계 메모

    // 응답 정보
    private RequestStatus status;
    private String responseMessage;

    // 시간 정보
    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
    private LocalDateTime expiresAt;

    /**
     * Entity -> DTO 변환 (바이탈 정보 없음)
     */
    public static HospitalRequestResponse from(HospitalRequest entity) {
        // JSON 문자열을 List<String>으로 변환
        List<String> symptomsList = parseSymptomsJson(entity.getSymptoms());

        return HospitalRequestResponse.builder()
                .id(entity.getId())
                .sessionCode(entity.getSessionCode())
                .patientCode(entity.getPatientCode())
                .hospitalId(entity.getHospital().getId())
                .hospitalName(entity.getHospital().getName())
                .hospitalAddress(entity.getHospital().getAddress())
                .distance(entity.getHospital().getDistance())
                .eta(entity.getHospital().getEta())
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
     * Entity + VitalData -> DTO 변환 (바이탈 정보 포함)
     */
    public static HospitalRequestResponse from(HospitalRequest entity, com.ems.copilot.emscopilot.domain.PatientVitalData vitalData) {
        HospitalRequestResponse response = from(entity);

        // Redis에서 조회한 바이탈 데이터 추가
        if (vitalData != null) {
            response.setSbp(vitalData.getSbp());
            response.setDbp(vitalData.getDbp());
            response.setHr(vitalData.getHr());
            response.setRr(vitalData.getRr());
            response.setSpo2(vitalData.getSpo2());
            response.setTemp(vitalData.getTemp());
            response.setParamedicMemo(vitalData.getParamedicMemo());
        }

        return response;
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
