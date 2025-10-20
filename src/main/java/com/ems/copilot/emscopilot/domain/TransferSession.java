package com.ems.copilot.emscopilot.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 환자 이송 요청 세션을 관리하는 엔티티(구급대원이 환자 생성할떄 생성된 세션 및 위치 ai분석 결과 등을 저장)
 * 이걸 기반으로 병원 요청 응답 보내서 HospitalResponse에 기록하고 최종적으로 매칭된 기록 Encounter로 관리
 */

@Entity
@Table(name = "transfer_sessions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransferSession {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "uuid2")
    @Column(columnDefinition = "VARCHAR(36)")
    private String id; // "a12345687-adf5464-" 이런식으로 온다.

    @Column(unique = true, nullable = false)
    private String sessionCode; // "P2025-001" 같은거

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @ElementCollection
    @CollectionTable(name = "session_chief_complaints",
            joinColumns = @JoinColumn(name = "session_id"))
    @Column(name = "complaint")
    @Builder.Default
    private List<String> chiefComplaint = new ArrayList<>();

    // 현재 위치 (더미 데이터)
    @Column(nullable = false)
    private String currentAddress; // "충청북도 음성군 음성읍 중앙로 195"

    @Column(precision = 10, nullable = false)
    private Double currentLatitude; // 36.9401

    @Column(precision = 10, nullable = false)
    private Double currentLongitude;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt; // 만료 시간 (30분 후)

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
