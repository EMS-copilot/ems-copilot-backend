package com.ems.copilot.emscopilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 병원에 환자 정보 전송 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendToHospitalsRequest {

    @NotBlank(message = "세션 ID는 필수입니다")
    private String sessionId;

    @NotEmpty(message = "최소 1개 이상의 병원을 선택해야 합니다")
    private List<Long> hospitalIds; // 선택한 병원 ID 목록
}
