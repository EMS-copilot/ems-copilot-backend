package com.ems.copilot.emscopilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * Speech-to-Text 변환 결과 DTO
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpeechToTextResponse {

    /**
     * 변환된 텍스트 (가장 정확도 높은 결과)
     */
    private String transcript;

    /**
     * 신뢰도 점수 (0.0 ~ 1.0)
     */
    private Float confidence;

    /**
     * 모든 대안 결과 목록
     */
    private List<Alternative> alternatives;

    /**
     * 변환 대안 결과
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Alternative {
        /**
         * 대안 텍스트
         */
        private String transcript;

        /**
         * 신뢰도 점수
         */
        private Float confidence;
    }
}
