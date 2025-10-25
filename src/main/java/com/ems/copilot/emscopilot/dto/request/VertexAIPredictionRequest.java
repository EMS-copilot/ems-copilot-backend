package com.ems.copilot.emscopilot.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Vertex AI Prediction API 표준 형식
 * 실제 요청 시 "instances" 배열로 감싸야 함
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VertexAIPredictionRequest {

    private List<VertexAIRequest> instances;
}
