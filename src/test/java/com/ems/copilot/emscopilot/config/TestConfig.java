package com.ems.copilot.emscopilot.config;

import com.ems.copilot.emscopilot.service.SpeechToTextService;
import com.ems.copilot.emscopilot.service.VertexAIService;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

/**
 * 테스트 환경에서 GCP 서비스들의 Mock Bean을 제공하는 설정
 */
@TestConfiguration
@Profile("test")
public class TestConfig {

    @Bean
    @Primary
    public VertexAIService vertexAIService() {
        return mock(VertexAIService.class);
    }

    @Bean
    @Primary
    public SpeechToTextService speechToTextService() {
        return mock(SpeechToTextService.class);
    }
}
