package com.ems.copilot.emscopilot.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.Arrays;
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
    private String id;

    @Column(unique = true, nullable = false)
    private String sessionCode;

    // 환자 식별 정보
    @Column(unique = true, nullable = false, length = 50)
    private String patientCode;

    @Column(nullable = false, length = 100)
    private String patientTempId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private SessionStatus status;

    @Column(name = "chief_complaints", length = 1000)
    private String chiefComplaints;

    // 현재 위치 (더미 데이터)
    @Column(nullable = false)
    private String currentAddress;

    @Column(precision = 10, nullable = false)
    private Double currentLatitude;

    @Column(precision = 10, nullable = false)
    private Double currentLongitude;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    // === 편의 메서드 ===

    /**
     * List를 쉼표로 구분된 문자열로 저장
     */
    public void setChiefComplaintList(List<String> complaints) {
        this.chiefComplaints = (complaints != null && !complaints.isEmpty())
            ? String.join(",", complaints)
            : "";
    }

    /**
     * 쉼표로 구분된 문자열을 List로 반환
     */
    public List<String> getChiefComplaintList() {
        if (chiefComplaints == null || chiefComplaints.isEmpty()) {
            return List.of();
        }
        return Arrays.asList(chiefComplaints.split(","));
    }

}
