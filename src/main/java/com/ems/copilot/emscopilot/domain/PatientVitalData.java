package com.ems.copilot.emscopilot.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Redis에 저장되는 환자 바이탈 정보
 * DB에는 저장되지 않음 (개인정보 보호)
 * 30분 후 자동 삭제 (TTL) 또는 Encounter 확정 시 삭제
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@RedisHash(value = "patientVital", timeToLive = 18000)  // 5시간
public class PatientVitalData {

    // 세션 정보 (Redis Key로 사용)
    @Id
    private String sessionId;

    // 환자 기본 정보 (메모리에만)
    private Integer age;
    private String sex;
    private Integer triageLevel;

    // 바이탈 사인 (메모리에만)
    private Integer sbp;        // 수축기 혈압
    private Integer dbp;        // 이완기 혈압
    private Integer hr;         // 심박수
    private Integer rr;         // 호흡수
    private Integer spo2;       // 산소포화도
    private Double temp;        // 체온

    // 증상
    private List<String> symptoms;

    // Redis TTL이 자동으로 30분 후 삭제 처리
    // expiresAt 필드 불필요 (Redis가 관리)
}
