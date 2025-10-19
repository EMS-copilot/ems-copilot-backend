package com.ems.copilot.emscopilot.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertexAIResponse {
    @JsonProperty("patient_id")
    private String patientId;

    @JsonProperty("result_method")
    private String resultMethod;

    private List<Prediction> predictions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Prediction {

        @JsonProperty("hospital_id")
        private String hospitalId;

        private Double score;

        private Map<String, Double> explanations;
    }
}
