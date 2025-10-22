package com.ems.copilot.emscopilot.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Encounter 확정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmEncounterRequest {

    @NotNull(message = "병원 요청 ID는 필수입니다")
    private Long hospitalRequestId; // 수용된 HospitalRequest ID
}
