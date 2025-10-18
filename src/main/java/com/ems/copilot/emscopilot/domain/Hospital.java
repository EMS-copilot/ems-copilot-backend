package com.ems.copilot.emscopilot.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "hospital")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Hospital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name; // 병원명

    @Column(length = 500)
    private String address; // 주소

    private Double latitude; // 위도 (hosp_lat)

    private Double longitude; // 경도 (hosp_lng)

    @Column(length = 20)
    private String phone; // 병원 대표전화

    @Min(0)
    @Max(20)
    private Integer icuBeds; // ICU 가용 병상 수 (0~20)

    @Min(0)
    @Max(20)
    private Integer erBeds; // 응급실 가용 병상 수 (0~20)

    private Integer availableBeds; // 일반 가용 병상 수

    @Column(nullable = false)
    private Boolean specialistOncall; // 전문의 당직 여부

    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private Integer hospitalCapacity; // 병원 수용률 (0~100)

    @Column(nullable = false)
    private Boolean status;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}