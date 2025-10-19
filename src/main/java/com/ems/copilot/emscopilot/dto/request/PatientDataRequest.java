package com.ems.copilot.emscopilot.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

@Data
public class PatientDataRequest {
    // 환자 기본 정보
    @NotNull(message = "나이는 필수입니다")
    @Min(value = 0) @Max(value = 150)
    private Integer age;

    @NotBlank(message = "성별은 필수입니다")
    @Pattern(regexp = "^(M|F|U)$", message = "성별은 M, F, U 중 하나여야 합니다")
    private String sex;

    @NotNull(message = "KTAS 등급은 필수입니다")
    @Min(value = 1) @Max(value = 5)
    private Integer triageLevel;

    // 바이탈 사인
    @NotNull(message = "수축기 혈압은 필수입니다")
    @Min(value = 0) @Max(value = 300)
    private Integer sbp;

    @NotNull(message = "이완기 혈압은 필수입니다")
    @Min(value = 0) @Max(value = 200)
    private Integer dbp;

    @NotNull(message = "심박수는 필수입니다")
    @Min(value = 0) @Max(value = 300)
    private Integer hr;

    @NotNull(message = "호흡수는 필수입니다")
    @Min(value = 0) @Max(value = 100)
    private Integer rr;

    @NotNull(message = "산소포화도는 필수입니다")
    @Min(value = 0) @Max(value = 100)
    private Integer spo2;

    @NotNull(message = "체온은 필수입니다")
    @DecimalMin("30.0") @DecimalMax("45.0")
    private Double temp;

    // 증상
    @NotBlank(message = "증상은 필수입니다")
    private List<String> symptoms; // "흉통", "호흡곤란" 등
}
