package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.dto.response.VertexAIResponse;
import jakarta.validation.constraints.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class VertexAIService {

    /**
     * Vertex AI 환자 분석 (현재는 Mock 데이터)
     * TODO: 실제 Vertex AI API 연동 시 수정
     */
    public VertexAIResponse analyzePatient(
            String patientId,
            Integer age,
            String sex,
            Integer triageLevel,
            Integer sbp,
            Integer hr,
            String symptom) {

        log.info("===== Mock Vertex AI 분석 시작 =====");
        log.info("환자 ID: {}", patientId);
        log.info("나이: {}, 성별: {}, KTAS: {}", age, sex, triageLevel);
        log.info("혈압: {}, 심박수: {}, 증상: {}", sbp, hr, symptom);

        // Mock 응답 생성
        return createMockResponse(patientId);
    }

    /**
     * Mock 응답 생성 (테스트용)
     */
    private VertexAIResponse createMockResponse(String patientId) {
        List<VertexAIResponse.Prediction> predictions = new ArrayList<>();

        // Mock 병원 1 - 충북대학교병원
        Map<String, Double> explanations1 = new HashMap<>();
        explanations1.put("eta", -0.12);
        explanations1.put("icu", 0.3);

        predictions.add(VertexAIResponse.Prediction.builder()
                .hospitalId("CBH_001")
                .score(0.87)
                .explanations(explanations1)
                .build());

        // Mock 병원 2 - 청주성모병원
        Map<String, Double> explanations2 = new HashMap<>();
        explanations2.put("specialist", 0.25);

        predictions.add(VertexAIResponse.Prediction.builder()
                .hospitalId("CBH_002")
                .score(0.72)
                .explanations(explanations2)
                .build());

        // Mock 병원 3 - 건국대 충주병원
        Map<String, Double> explanations3 = new HashMap<>();
        explanations3.put("distance", 0.15);

        predictions.add(VertexAIResponse.Prediction.builder()
                .hospitalId("CBH_003")
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