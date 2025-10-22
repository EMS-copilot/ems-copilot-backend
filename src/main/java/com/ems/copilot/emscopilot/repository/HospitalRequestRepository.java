package com.ems.copilot.emscopilot.repository;

import com.ems.copilot.emscopilot.domain.HospitalRequest;
import com.ems.copilot.emscopilot.domain.RequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
     * 세션 코드와 병원 ID로 PENDING 상태의 요청 조회 (병원 응답용)
     */
    Optional<HospitalRequest> findBySessionCodeAndHospitalIdAndStatus(String sessionCode, Long hospitalId, RequestStatus status);
}
