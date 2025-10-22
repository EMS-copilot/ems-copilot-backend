package com.ems.copilot.emscopilot.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 병원 수용 요청
 *
 * 구급대원이 선택한 병원에 환자 간단 정보를 전송한 기록
 * DB에는 간단한 정보만 저장 (개인정보 최소화)
 * 전체 바이탈 정보는 Redis에만 저장
 *
 * 라이프사이클:
 * 1. 구급대원이 병원 선택 → HospitalRequest 생성 (status: PENDING)
 * 2. 병원이 수용/거절 → status 업데이트 (ACCEPTED/REJECTED)
 * 3. 30분 경과 → status: EXPIRED
 */
@Entity
@Table(name = "hospital_requests")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HospitalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 세션 정보
    @Column(nullable = false, length = 50)
    private String sessionId; // Redis에서 바이탈 정보 조회용

    // 병원 정보
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    // 환자 간단 정보 (DB 저장 - 병원이 빠르게 확인용)
    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 1)
    private String sex; // M, F, U

    @Column(nullable = false)
    private Integer triageLevel; // KTAS 1-5

    // 증상 (JSON 문자열로 저장)
    @Column(columnDefinition = "TEXT")
    private String symptoms; // ["복통", "구토", "발열"] 형태의 JSON

    // 요청 상태 및 응답
    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private RequestStatus status; // PENDING, ACCEPTED, REJECTED, EXPIRED

    @Column(length = 500)
    private String responseMessage; // 병원의 응답 메시지

    // 시간 정보
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 요청 시간

    @Column
    private LocalDateTime respondedAt; // 병원 응답 시간

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 만료 시간 (30분 후)

    /**
     * 만료 여부 확인
     */
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    /**
     * 병원 응답 처리
     */
    public void respond(RequestStatus status, String message) {
        this.status = status;
        this.responseMessage = message;
        this.respondedAt = LocalDateTime.now();
    }
}
