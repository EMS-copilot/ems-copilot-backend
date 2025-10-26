package com.ems.copilot.emscopilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

/**
 * Speech-to-Text 변환 요청 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SpeechToTextRequest {

    /**
     * 음성 파일
     */
    @NotNull(message = "음성 파일은 필수입니다.")
    private MultipartFile audioFile;

    /**
     * 언어 코드 (기본값: ko-KR)
     * 예: ko-KR (한국어), en-US (영어), ja-JP (일본어)
     */
    private String languageCode = "ko-KR";

    /**
     * 샘플링 속도 (Hz)
     * 기본값: 16000
     * 오디오 파일의 실제 샘플링 속도와 일치해야 함
     */
    private Integer sampleRateHertz = 16000;

    /**
     * 오디오 인코딩
     * 기본값: LINEAR16
     * 가능한 값: LINEAR16, FLAC, MULAW, AMR, AMR_WB, OGG_OPUS, SPEEX_WITH_HEADER_BYTE, MP3, WEBM_OPUS
     */
    private String encoding = "LINEAR16";
}
