package com.ems.copilot.emscopilot.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

/**
 * vertex ai에게 전송하는 데이터
 */

@Data
@Builder
public class VertexAIRequest {

    @JsonProperty("patient_id")
    private String patientId;

    private Integer age;

    private String sex;

    @JsonProperty("triage_level")
    private Integer triageLevel;

    @JsonProperty("bp_systolic")
    private Integer bpSystolic;

    private Integer hr;

    private String symptom;
}
