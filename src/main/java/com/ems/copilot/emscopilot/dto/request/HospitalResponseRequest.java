package com.ems.copilot.emscopilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 병원 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponseRequest {

    @NotBlank(message = "응답은 필수입니다")
    @Pattern(regexp = "^(ACCEPTED|REJECTED)$", message = "응답은 ACCEPTED 또는 REJECTED만 가능합니다")
    private String response; // ACCEPTED, REJECTED

    private String message; // 응답 메시지 (거절 사유 등)
}
