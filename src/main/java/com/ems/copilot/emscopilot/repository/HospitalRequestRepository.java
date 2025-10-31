package com.ems.copilot.emscopilot.repository;

import com.ems.copilot.emscopilot.domain.HospitalRequest;
import com.ems.copilot.emscopilot.domain.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRequestRepository extends JpaRepository<HospitalRequest, Long> {

    /**
     * 세션 코드로 모든 요청 조회
     */
    List<HospitalRequest> findBySessionCode(String sessionCode);

    /**
     * 세션 코드와 상태로 요청 조회
     */
    List<HospitalRequest> findBySessionCodeAndStatus(String sessionCode, RequestStatus status);

    /**
     * 병원 ID로 요청 조회
     */
    List<HospitalRequest> findByHospitalId(Long hospitalId);

    /**
     * 병원 ID와 상태로 요청 조회
     */
    List<HospitalRequest> findByHospitalIdAndStatus(Long hospitalId, RequestStatus status);

    /**
     * 세션 코드와 병원 ID로 PENDING 상태의 요청 조회 (병원 응답용)
     */
    Optional<HospitalRequest> findBySessionCodeAndHospitalIdAndStatus(String sessionCode, Long hospitalId, RequestStatus status);

    /**
     * 구급대원 ID로 요청 목록 조회 (최신순)
     */
    List<HospitalRequest> findByParamedicIdOrderByCreatedAtDesc(Long paramedicId);

    /**
     * 구급대원 ID와 상태로 요청 조회
     */
    List<HospitalRequest> findByParamedicIdAndStatus(Long paramedicId, RequestStatus status);

    /**
     * 특정 기간 동안 특정 병원의 특정 상태 건수 조회 (랭킹 계산용)
     */
    @Query("SELECT COUNT(hr) FROM HospitalRequest hr WHERE hr.hospital.id = :hospitalId " +
           "AND hr.status = :status AND hr.createdAt >= :since")
    long countByHospitalIdAndStatusAndCreatedAtAfter(
            @Param("hospitalId") Long hospitalId,
            @Param("status") RequestStatus status,
            @Param("since") LocalDateTime since);
}
