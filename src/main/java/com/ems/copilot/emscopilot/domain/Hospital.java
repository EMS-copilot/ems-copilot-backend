package com.ems.copilot.emscopilot.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
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

    @Column(unique = true, nullable = false)
    private String externalId; // CBH-001

    @Column(nullable = false, length = 200)
    private String name; // 병원명

    @Column(length = 500)
    private String address; // 주소

    private Double distance;

    private Integer eta;

    private Integer icuBeds;

    private Integer erBeds;

    @Column(name = "grade", length = 1)
    private Character rank;

    @Column(nullable = false)
    @Min(0)
    @Max(100)
    private Integer hospitalCapacity; // 병원 수용률 (0~100)

    @Column(length = 500)
    private String description;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    /**
     * 병원 랭크 업데이트
     *
     * @param rank 새로운 등급 (A~F)
     */
    public void updateRank(Character rank) {
        this.rank = rank;
    }
}