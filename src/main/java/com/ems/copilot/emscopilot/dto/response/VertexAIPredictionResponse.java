package com.ems.copilot.emscopilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Vertex AI Prediction API 표준 응답 형식
 * 실제 응답은 "predictions" 배열로 감싸져 있음
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertexAIPredictionResponse {

    private List<VertexAIResponse> predictions;
}
