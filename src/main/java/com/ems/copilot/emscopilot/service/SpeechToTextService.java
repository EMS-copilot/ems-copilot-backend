package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.dto.response.SpeechToTextResponse;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.speech.v1.*;
import com.google.protobuf.ByteString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Google Cloud Speech-to-Text 서비스
 * 음성 파일을 텍스트로 변환하는 기능을 제공합니다.
 */
@Service
@Slf4j
public class SpeechToTextService {

    private final SpeechClient speechClient;
    private final String keyFilePath;
    private final ResourceLoader resourceLoader;

    /**
     * SpeechClient를 생성자에서 초기화합니다.
     * GCP 서비스 계정 키 파일을 사용하여 인증합니다.
     */
    public SpeechToTextService(
            ResourceLoader resourceLoader,
            @Value("${gcp.service-account.key-path}") String keyFilePath) throws IOException {
        this.resourceLoader = resourceLoader;
        this.keyFilePath = keyFilePath;
        this.speechClient = createSpeechClient();
        log.info("SpeechClient 초기화 완료");
    }

    /**
     * 서비스 계정 키 파일을 사용하여 SpeechClient 생성
     */
    private SpeechClient createSpeechClient() throws IOException {
        log.info("SpeechClient 초기화 시작 - 키 파일 경로: {}", keyFilePath);

        try {
            Resource resource = resourceLoader.getResource(keyFilePath);

            if (!resource.exists()) {
                throw new IOException("서비스 계정 키 파일을 찾을 수 없습니다: " + keyFilePath);
            }

            GoogleCredentials credentials;
            try (InputStream serviceAccountStream = resource.getInputStream()) {
                credentials = GoogleCredentials.fromStream(serviceAccountStream)
                        .createScoped("https://www.googleapis.com/auth/cloud-platform");
            }

            SpeechSettings settings = SpeechSettings.newBuilder()
                    .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                    .build();

            return SpeechClient.create(settings);

        } catch (IOException e) {
            log.error("SpeechClient 초기화 실패: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * 짧은 오디오 파일(1분 미만)을 동기 방식으로 텍스트로 변환합니다.
     *
     * @param audioFile      음성 파일
     * @param languageCode   언어 코드 (예: "ko-KR", "en-US")
     * @param sampleRateHz   샘플링 속도 (Hz)
     * @param encodingType   오디오 인코딩 타입
     * @return 변환된 텍스트 및 신뢰도 정보
     */
    public SpeechToTextResponse transcribeAudio(
            MultipartFile audioFile,
            String languageCode,
            int sampleRateHz,
            String encodingType) throws IOException {

        log.info("===== Speech-to-Text 변환 시작 =====");
        log.info("파일명: {}, 크기: {} bytes", audioFile.getOriginalFilename(), audioFile.getSize());
        log.info("언어 코드: {}, 샘플링 속도: {}Hz, 인코딩: {}", languageCode, sampleRateHz, encodingType);

        // 오디오 파일을 ByteString으로 변환
        byte[] audioBytes = audioFile.getBytes();
        ByteString audioData = ByteString.copyFrom(audioBytes);

        // 오디오 인코딩 설정
        RecognitionConfig.AudioEncoding encoding = parseAudioEncoding(encodingType);

        // 오디오 설정
        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(encoding)
                .setSampleRateHertz(sampleRateHz)
                .setLanguageCode(languageCode)
                .setEnableAutomaticPunctuation(true) // 자동 문장 부호 추가
                .build();

        // 오디오 데이터
        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setContent(audioData)
                .build();

        // API 호출
        RecognizeResponse response = speechClient.recognize(config, audio);

        // 결과 처리
        List<SpeechRecognitionResult> results = response.getResultsList();

        if (results.isEmpty()) {
            log.warn("변환된 텍스트가 없습니다.");
            return SpeechToTextResponse.builder()
                    .transcript("")
                    .confidence(0.0f)
                    .alternatives(List.of())
                    .build();
        }

        // 가장 정확도가 높은 첫 번째 결과
        SpeechRecognitionAlternative topAlternative = results.get(0).getAlternatives(0);
        String transcript = topAlternative.getTranscript();
        float confidence = topAlternative.getConfidence();

        // 모든 대안 결과 수집
        List<SpeechToTextResponse.Alternative> alternatives = results.stream()
                .flatMap(result -> result.getAlternativesList().stream())
                .map(alt -> SpeechToTextResponse.Alternative.builder()
                        .transcript(alt.getTranscript())
                        .confidence(alt.getConfidence())
                        .build())
                .collect(Collectors.toList());

        log.info("===== Speech-to-Text 변환 완료 =====");
        log.info("변환 결과: {}", transcript);
        log.info("신뢰도: {}", confidence);

        return SpeechToTextResponse.builder()
                .transcript(transcript)
                .confidence(confidence)
                .alternatives(alternatives)
                .build();
    }

    /**
     * 긴 오디오 파일(GCS에 저장된 파일)을 비동기 방식으로 텍스트로 변환합니다.
     *
     * @param gcsUri       Google Cloud Storage URI (예: gs://my-bucket/audio.flac)
     * @param languageCode 언어 코드
     * @param sampleRateHz 샘플링 속도
     * @param encodingType 오디오 인코딩 타입
     * @return 변환된 텍스트 (작업 완료까지 블로킹)
     */
    public SpeechToTextResponse transcribeLongAudioGcs(
            String gcsUri,
            String languageCode,
            int sampleRateHz,
            String encodingType) throws Exception {

        log.info("===== Speech-to-Text 장시간 변환 시작 (GCS) =====");
        log.info("GCS URI: {}", gcsUri);
        log.info("언어 코드: {}, 샘플링 속도: {}Hz, 인코딩: {}", languageCode, sampleRateHz, encodingType);

        // 오디오 인코딩 설정
        RecognitionConfig.AudioEncoding encoding = parseAudioEncoding(encodingType);

        RecognitionConfig config = RecognitionConfig.newBuilder()
                .setEncoding(encoding)
                .setSampleRateHertz(sampleRateHz)
                .setLanguageCode(languageCode)
                .setEnableAutomaticPunctuation(true)
                .build();

        RecognitionAudio audio = RecognitionAudio.newBuilder()
                .setUri(gcsUri)
                .build();

        // 비동기 작업 요청
        OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> responseFuture =
                speechClient.longRunningRecognizeAsync(config, audio);

        log.info("비동기 작업 시작. 완료될 때까지 대기 중...");

        // 작업 완료 대기
        LongRunningRecognizeResponse response = responseFuture.get();

        List<SpeechRecognitionResult> results = response.getResultsList();

        if (results.isEmpty()) {
            log.warn("변환된 텍스트가 없습니다.");
            return SpeechToTextResponse.builder()
                    .transcript("")
                    .confidence(0.0f)
                    .alternatives(List.of())
                    .build();
        }

        // 모든 결과를 하나의 텍스트로 결합
        StringBuilder transcriptBuilder = new StringBuilder();
        float totalConfidence = 0.0f;
        int count = 0;

        for (SpeechRecognitionResult result : results) {
            SpeechRecognitionAlternative alternative = result.getAlternatives(0);
            transcriptBuilder.append(alternative.getTranscript()).append(" ");
            totalConfidence += alternative.getConfidence();
            count++;
        }

        String fullTranscript = transcriptBuilder.toString().trim();
        float avgConfidence = count > 0 ? totalConfidence / count : 0.0f;

        // 모든 대안 결과 수집
        List<SpeechToTextResponse.Alternative> alternatives = results.stream()
                .flatMap(result -> result.getAlternativesList().stream())
                .map(alt -> SpeechToTextResponse.Alternative.builder()
                        .transcript(alt.getTranscript())
                        .confidence(alt.getConfidence())
                        .build())
                .collect(Collectors.toList());

        log.info("===== Speech-to-Text 장시간 변환 완료 =====");
        log.info("변환 결과 길이: {} 문자", fullTranscript.length());
        log.info("평균 신뢰도: {}", avgConfidence);

        return SpeechToTextResponse.builder()
                .transcript(fullTranscript)
                .confidence(avgConfidence)
                .alternatives(alternatives)
                .build();
    }

    /**
     * 문자열을 AudioEncoding enum으로 변환
     */
    private RecognitionConfig.AudioEncoding parseAudioEncoding(String encodingType) {
        try {
            return RecognitionConfig.AudioEncoding.valueOf(encodingType.toUpperCase());
        } catch (IllegalArgumentException e) {
            log.warn("지원하지 않는 인코딩 타입: {}. LINEAR16으로 기본 설정됩니다.", encodingType);
            return RecognitionConfig.AudioEncoding.LINEAR16;
        }
    }

    /**
     * 서비스 종료 시 리소스 정리
     */
    public void shutdown() {
        if (speechClient != null) {
            speechClient.close();
            log.info("SpeechClient 종료됨");
        }
    }
}
