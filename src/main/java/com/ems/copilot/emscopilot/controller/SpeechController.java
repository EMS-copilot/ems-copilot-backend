package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.SpeechToTextResponse;
import com.ems.copilot.emscopilot.service.SpeechToTextService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * Speech-to-Text API 컨트롤러
 * 음성 파일을 텍스트로 변환하는 API를 제공합니다.
 */
@RestController
@RequestMapping("/api/speech")
@RequiredArgsConstructor
@Slf4j
public class SpeechController {

    private final SpeechToTextService speechToTextService;

    /**
     * 짧은 오디오 파일(1분 미만)을 텍스트로 변환합니다.
     *
     * @param audioFile      음성 파일 (MultipartFile)
     * @param languageCode   언어 코드 (기본값: ko-KR)
     * @param sampleRateHz   샘플링 속도 (기본값: 16000)
     * @param encoding       오디오 인코딩 (기본값: LINEAR16)
     * @return 변환된 텍스트 및 신뢰도 정보
     */
    @PostMapping("/transcribe")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<SpeechToTextResponse>> transcribeAudio(
            @RequestParam("audioFile") MultipartFile audioFile,
            @RequestParam(value = "languageCode", defaultValue = "ko-KR") String languageCode,
            @RequestParam(value = "sampleRateHz", defaultValue = "16000") Integer sampleRateHz,
            @RequestParam(value = "encoding", defaultValue = "LINEAR16") String encoding) {

        log.info("==== 음성 파일 변환 요청 ====");
        log.info("파일명: {}, 크기: {} bytes", audioFile.getOriginalFilename(), audioFile.getSize());
        log.info("언어 코드: {}, 샘플링 속도: {}Hz, 인코딩: {}", languageCode, sampleRateHz, encoding);

        try {
            // 파일 크기 검증 (선택적)
            if (audioFile.isEmpty()) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        "ERROR",
                        "음성 파일이 비어있습니다.",
                        null
                ));
            }

            // Speech-to-Text 변환
            SpeechToTextResponse result = speechToTextService.transcribeAudio(
                    audioFile,
                    languageCode,
                    sampleRateHz,
                    encoding
            );

            log.info("음성 변환 완료 - 변환된 텍스트: {}", result.getTranscript());

            ApiResponse<SpeechToTextResponse> response = new ApiResponse<>(
                    "SUCCESS",
                    "음성 파일이 성공적으로 텍스트로 변환되었습니다.",
                    result
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("음성 변환 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(
                    "ERROR",
                    "음성 변환 중 오류가 발생했습니다: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * GCS에 저장된 긴 오디오 파일을 텍스트로 변환합니다. (비동기)
     *
     * @param gcsUri         GCS URI (예: gs://my-bucket/audio.flac)
     * @param languageCode   언어 코드 (기본값: ko-KR)
     * @param sampleRateHz   샘플링 속도 (기본값: 16000)
     * @param encoding       오디오 인코딩 (기본값: FLAC)
     * @return 변환된 텍스트 및 신뢰도 정보
     */
    @PostMapping("/transcribe/gcs")
    @PreAuthorize("hasAnyRole('PARAMEDIC_ADMIN', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<SpeechToTextResponse>> transcribeGcsAudio(
            @RequestParam("gcsUri") String gcsUri,
            @RequestParam(value = "languageCode", defaultValue = "ko-KR") String languageCode,
            @RequestParam(value = "sampleRateHz", defaultValue = "16000") Integer sampleRateHz,
            @RequestParam(value = "encoding", defaultValue = "FLAC") String encoding) {

        log.info("==== GCS 음성 파일 변환 요청 ====");
        log.info("GCS URI: {}", gcsUri);
        log.info("언어 코드: {}, 샘플링 속도: {}Hz, 인코딩: {}", languageCode, sampleRateHz, encoding);

        try {
            // GCS URI 검증
            if (!gcsUri.startsWith("gs://")) {
                return ResponseEntity.badRequest().body(new ApiResponse<>(
                        "ERROR",
                        "올바른 GCS URI 형식이 아닙니다. (예: gs://bucket-name/file-name)",
                        null
                ));
            }

            // Speech-to-Text 변환 (비동기)
            SpeechToTextResponse result = speechToTextService.transcribeLongAudioGcs(
                    gcsUri,
                    languageCode,
                    sampleRateHz,
                    encoding
            );

            log.info("GCS 음성 변환 완료 - 변환된 텍스트 길이: {} 문자", result.getTranscript().length());

            ApiResponse<SpeechToTextResponse> response = new ApiResponse<>(
                    "SUCCESS",
                    "GCS 음성 파일이 성공적으로 텍스트로 변환되었습니다.",
                    result
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("GCS 음성 변환 실패: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(new ApiResponse<>(
                    "ERROR",
                    "GCS 음성 변환 중 오류가 발생했습니다: " + e.getMessage(),
                    null
            ));
        }
    }

    /**
     * Speech-to-Text API 상태 확인
     */
    @GetMapping("/health")
    public ResponseEntity<ApiResponse<String>> healthCheck() {
        log.info("Speech-to-Text API 상태 확인");
        return ResponseEntity.ok(new ApiResponse<>(
                "SUCCESS",
                "Speech-to-Text API가 정상 작동 중입니다.",
                "OK"
        ));
    }
}
