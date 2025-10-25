package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.dto.request.VertexAIRequest;
import com.ems.copilot.emscopilot.dto.response.VertexAIResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VertexAIService {

    private final VertexAiClient vertexAiClient;
    private final Gson gson;

    @Value("${gcp.vertex-ai.use-mock:false}")
    private boolean useMock;

    public VertexAIService(VertexAiClient vertexAiClient) {
        this.vertexAiClient = vertexAiClient;
        this.gson = new Gson();
    }

    /**
     * Vertex AI 환자 분석
     * Vertex AI API를 호출하여 병원 추천 결과를 받습니다.
     */
    public VertexAIResponse analyzePatient(VertexAIRequest request) {

        log.info("===== Vertex AI 분석 시작 =====");
        log.info("환자 ID: {}", request.getPatient().getId());
        log.info("나이: {}, 성별: {}, KTAS: {}",
                request.getPatient().getAge(),
                request.getPatient().getSex(),
                request.getPatient().getTriageLevel());
        log.info("혈압: {}, 심박수: {}, 증상: {}",
                request.getPatient().getBpSystolic(),
                request.getPatient().getHr(),
                request.getPatient().getSymptom());
        log.info("후보 병원 수: {}", request.getCandidateHospitals().size());
        log.info("TopK: {}", request.getResultMethod().getTopK());

        // Mock 모드가 활성화되어 있으면 Mock 응답 반환
        if (useMock) {
            log.info("Mock 모드 활성화 - Mock 응답 반환");
            return createMockResponse(request.getPatient().getId());
        }

        try {
            // 실제 Vertex AI API 호출
            String responseJson = vertexAiClient.predict(request);

            // JSON 응답을 DTO로 변환
            VertexAIResponse response = gson.fromJson(responseJson, VertexAIResponse.class);

            log.info("===== Vertex AI 분석 완료 =====");
            log.info("추천 병원 수: {}", response.getPredictions().size());

            return response;

        } catch (Exception e) {
            log.error("Vertex AI API 호출 실패, Mock 응답 반환: {}", e.getMessage());
            // API 호출 실패 시 Mock 응답 반환 (Fallback)
            return createMockResponse(request.getPatient().getId());
        }
    }

    /**
     * Mock 응답 생성 (테스트용)
     */
    private VertexAIResponse createMockResponse(String patientId) {
        List<VertexAIResponse.Prediction> predictions = new ArrayList<>();

        // Mock 병원 1
        Map<String, Double> explanations1 = new HashMap<>();
        explanations1.put("eta", -0.12);
        explanations1.put("icu", 0.3);

        predictions.add(VertexAIResponse.Prediction.builder()
                .hospitalId("CBH-001")
                .score(0.87)
                .explanations(explanations1)
                .build());

        // Mock 병원 2
        Map<String, Double> explanations2 = new HashMap<>();
        explanations2.put("specialist", 0.25);

        predictions.add(VertexAIResponse.Prediction.builder()
                .hospitalId("CBH-002")
                .score(0.72)
                .explanations(explanations2)
                .build());

        // Mock 병원 3
        Map<String, Double> explanations3 = new HashMap<>();
        explanations3.put("distance", 0.15);

        predictions.add(VertexAIResponse.Prediction.builder()
                .hospitalId("CBH-003")
                .score(0.63)
                .explanations(explanations3)
                .build());

        log.info("Mock 응답 생성 완료 - 추천 병원 {}개", predictions.size());

        return VertexAIResponse.builder()
                .patientId(patientId)
                .resultMethod("topK")
                .predictions(predictions)
                .build();
    }
}