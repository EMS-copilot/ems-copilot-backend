package com.ems.copilot.emscopilot.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 구급대원 메모 저장/수정 요청 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemoUpdateRequest {

    @NotBlank(message = "메모 내용은 필수입니다")
    private String memo;
}
