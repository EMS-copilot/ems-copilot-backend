package com.ems.copilot.emscopilot.service;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.io.IOException;
import java.time.Duration;

/**
 * Vertex AI Prediction API 클라이언트
 * Google Cloud의 Vertex AI 엔드포인트에 예측 요청을 보냅니다.
 */
@Service
@Profile("!test")
@Slf4j
public class VertexAiClient {

    private final GcpAuthService authService;
    private final WebClient webClient;
    private final Gson gson;

    @Value("${gcp.vertex-ai.endpoint-url}")
    private String endpointUrl;

    public VertexAiClient(GcpAuthService authService, WebClient.Builder webClientBuilder) {
        this.authService = authService;
        this.webClient = webClientBuilder
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .codecs(configurer -> configurer
                        .defaultCodecs()
                        .maxInMemorySize(10 * 1024 * 1024)) // 10MB
                .build();
        this.gson = new Gson();
    }

    /**
     * Vertex AI Prediction Endpoint에 예측 요청을 보냅니다.
     *
     * @param requestBody 예측 인스턴스를 담은 요청 객체 (DTO)
     * @return 응답 JSON 문자열
     * @throws IOException GCP 인증 실패 시
     * @throws RuntimeException API 호출 실패 시
     */
    public String predict(Object requestBody) throws IOException {

        log.info("===== Vertex AI API 호출 시작 =====");
        log.info("Endpoint URL: {}", endpointUrl);

        // 1. 유효한 Access Token 획득
        String accessToken = authService.getAccessToken();
        log.debug("Access Token 획득 완료");

        // 2. 요청 본문을 Vertex AI 표준 형식으로 변환
        // Vertex AI는 { "instances": [...] } 형식을 요구함
        String requestJson;
        if (requestBody instanceof String) {
            requestJson = (String) requestBody;
        } else {
            // instances 배열로 감싸기
            requestJson = String.format("{\"instances\": [%s]}", gson.toJson(requestBody));
        }
        log.info("요청 Body: {}", requestJson);

        // 3. WebClient를 사용하여 요청 구성 및 전송
        try {
            String response = webClient.post()
                    .uri(endpointUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30)) // 30초 타임아웃
                    .block();

            log.info("===== Vertex AI API 호출 성공 =====");
            log.debug("응답 Body: {}", response);

            return response;

        } catch (WebClientResponseException e) {
            log.error("Vertex AI API 호출 실패 - HTTP Status: {}", e.getStatusCode());
            log.error("응답 Body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Vertex AI API 호출 실패: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Vertex AI API 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Vertex AI API 호출 중 오류 발생", e);
        }
    }

    /**
     * JSON 문자열을 직접 전달하는 예측 메서드
     *
     * @param requestJson 예측 인스턴스를 담은 JSON 문자열
     * @return 응답 JSON 문자열
     */
    public String predictWithJson(String requestJson) throws IOException {

        log.info("===== Vertex AI API 호출 시작 (JSON) =====");
        log.info("Endpoint URL: {}", endpointUrl);

        // 유효한 Access Token 획득
        String accessToken = authService.getAccessToken();

        try {
            String response = webClient.post()
                    .uri(endpointUrl)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestJson)
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(30))
                    .block();

            log.info("===== Vertex AI API 호출 성공 =====");
            return response;

        } catch (WebClientResponseException e) {
            log.error("Vertex AI API 호출 실패 - HTTP Status: {}", e.getStatusCode());
            log.error("응답 Body: {}", e.getResponseBodyAsString());
            throw new RuntimeException("Vertex AI API 호출 실패: " + e.getMessage(), e);

        } catch (Exception e) {
            log.error("Vertex AI API 호출 중 예외 발생: {}", e.getMessage(), e);
            throw new RuntimeException("Vertex AI API 호출 중 오류 발생", e);
        }
    }
}
