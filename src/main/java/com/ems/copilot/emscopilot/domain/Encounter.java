package com.ems.copilot.emscopilot.domain;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 환자-병원 매칭 완료 기록 (영구 보관)
 *
 * 개인정보 보호 원칙:
 * - 바이탈 정보는 DB에 저장하지 않음 (Redis에서만 임시 사용)
 * - 최소한의 정보만 저장: 환자 식별, 병원, 이송 정보
 * - 응급의료법 제50조: 이송 기록 보존 의무 (바이탈 제외)
 */

@Entity
@Table(name = "encounters")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Encounter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 환자 식별 정보
    @Column(nullable = false, unique = true, length = 50)
    private String patientCode; // "PT2025-002" (사람이 읽기 쉬운 코드)

    @Column(nullable = false, length = 100)
    private String patientTempId; // "patient_xxx..." (시스템 내부 ID)

    // 세션 정보 (참조용)
    @Column(nullable = false, length = 50)
    private String sessionCode; // "S2025-002" (TransferSession 참조)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paramedic_id", nullable = false)
    private User paramedic;

    // 매칭된 병원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;

    // 환자 정보 (최소한의 정보만)
    @Column(nullable = false)
    private Integer age;

    @Column(nullable = false, length = 1)
    private String sex; // M, F, U

    @Column(nullable = false)
    private Integer triageLevel; // KTAS 1-5

    // 바이탈 정보는 개인정보 보호를 위해 DB에 저장하지 않음
    // Redis에서만 임시 사용 후 병원 완료 시 삭제

    // 이송 정보
    @Column(nullable = false)
    private String transferLocation; // 이송 출발 주소

    @Column(nullable = false)
    private Double transferDistance; // 이송 거리 (km)

    @Column(nullable = false)
    private Integer transferEta; // 예상 소요 시간 (분)

    // AI 분석 정보
    @Column(nullable = false)
    private Double aiScore; // Vertex AI가 부여한 점수

    @Column(nullable = false)
    private Integer aiPriority; // 추천 순위

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private SessionStatus status; // PENDING, TRANSFERRED, COMPLETED, CANCELLED

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt; // 매칭된 시간

    @Column
    private LocalDateTime transferredAt; // 실제 이송 시작 시간

    @Column
    private LocalDateTime completedAt; // 이송 완료 시간

    // 비고
    @Column(length = 1000)
    private String notes;
}
