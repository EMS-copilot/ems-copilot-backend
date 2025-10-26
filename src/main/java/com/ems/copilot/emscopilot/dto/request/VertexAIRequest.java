package com.ems.copilot.emscopilot.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * vertex ai에게 전송하는 데이터
 */

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertexAIRequest {

    private Patient patient;

    @JsonProperty("candidate_hospitals")
    private List<CandidateHospital> candidate_hospitals;

    @JsonProperty("result_method")
    private ResultMethod result_method;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patient {
        private String id;
        private Integer age;
        private String sex;

        @JsonProperty("triage_level")
        private Integer triage_level;

        private String symptom;

        @JsonProperty("bp_systolic")
        private Integer bp_systolic;

        private Integer hr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateHospital {
        @JsonProperty("hospital_id")
        private String hospital_id;

        @JsonProperty("hospital_capacity")
        private Integer hospital_capacity;

        @JsonProperty("icu_beds")
        private Integer icu_beds;

        @JsonProperty("er_beds")
        private Integer er_beds;

        @JsonProperty("distance_km")
        private Double distance_km;

        @JsonProperty("eta_minutes")
        private Integer eta_minutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultMethod {
        private Integer topK;
    }
}
