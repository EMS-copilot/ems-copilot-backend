package com.ems.copilot.emscopilot.service;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

/**
 * Google Cloud Platform 인증 서비스
 * 서비스 계정 키 파일을 사용하여 Access Token을 획득하고 관리합니다.
 */
@Service
@Profile("!test")
@Slf4j
public class GcpAuthService {

    // 서비스 계정 키 파일 경로 (application.yml에서 주입)
    @Value("${gcp.service-account.key-path}")
    private String keyFilePath;

    private final ResourceLoader resourceLoader;

    // Vertex AI 접근에 필요한 OAuth2 Scope
    private static final List<String> SCOPES =
            Collections.singletonList("https://www.googleapis.com/auth/cloud-platform");

    private GoogleCredentials credentials;

    public GcpAuthService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    /**
     * Google Cloud API 호출에 사용할 Access Token을 획득하거나 갱신합니다.
     * 토큰이 만료되었으면 자동으로 갱신합니다.
     *
     * @return 유효한 Access Token 문자열
     * @throws IOException 인증 파일 로드 실패 시 발생
     */
    public String getAccessToken() throws IOException {

        // Credentials 객체가 초기화되지 않았으면 초기화
        if (credentials == null) {
            initializeCredentials();
        }

        // 토큰이 만료되었는지 확인하고 만료되었다면 자동으로 갱신
        credentials.refreshIfExpired();

        String token = credentials.getAccessToken().getTokenValue();
        log.debug("Access Token 획득 완료 (길이: {})", token.length());

        return token;
    }

    /**
     * Google Credentials 초기화
     * 서비스 계정 키 파일을 로드하고 필요한 Scope를 설정합니다.
     */
    private void initializeCredentials() throws IOException {
        log.info("GCP 인증 초기화 시작 - 키 파일 경로: {}", keyFilePath);

        try {
            Resource resource = resourceLoader.getResource(keyFilePath);

            if (!resource.exists()) {
                throw new IOException("서비스 계정 키 파일을 찾을 수 없습니다: " + keyFilePath);
            }

            try (InputStream serviceAccountStream = resource.getInputStream()) {
                credentials = ServiceAccountCredentials.fromStream(serviceAccountStream)
                        .createScoped(SCOPES);
            }

            log.info("GCP 인증 초기화 완료");

        } catch (IOException e) {
            log.error("GCP 인증 초기화 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Credentials 재초기화 (테스트 또는 키 파일 변경 시 사용)
     */
    public void resetCredentials() {
        credentials = null;
        log.info("Credentials가 리셋되었습니다.");
    }
}
