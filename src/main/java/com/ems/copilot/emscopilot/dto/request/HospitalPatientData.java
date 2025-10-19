package com.ems.copilot.emscopilot.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 병원측에 전송하는 데이터
 */
@Data
@Builder
public class HospitalPatientData {

    @JsonProperty("session_code")
    private String sessionCode;

    @JsonProperty("triage_level")
    private String triageLevel;

    private List<String> symptoms;

    // 바이탈 사인
    private Integer sbp;
    private Integer dbp;
    private Integer hr;
    private Integer rr;
    private Integer spo2;
}
