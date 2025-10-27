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
    private List<CandidateHospital> candidateHospitals;

    @JsonProperty("result_method")
    private ResultMethod resultMethod;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Patient {
        private String id;
        private Integer age;
        private String sex;

        @JsonProperty("triage_level")
        private Integer triageLevel;

        private String symptom;

        @JsonProperty("bp_systolic")
        private Integer bpSystolic;

        private Integer hr;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CandidateHospital {
        @JsonProperty("hospital_id")
        private String hospitalId;

        @JsonProperty("hospital_capacity")
        private Integer hospitalCapacity;

        @JsonProperty("icu_beds")
        private Integer icuBeds;

        @JsonProperty("er_beds")
        private Integer erBeds;

        @JsonProperty("distance_km")
        private Double distanceKm;

        @JsonProperty("eta_minutes")
        private Integer etaMinutes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResultMethod {
        private Integer topK;
    }
}
