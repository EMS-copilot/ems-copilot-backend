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
     * 세션 ID로 모든 요청 조회
     */
    List<HospitalRequest> findBySessionId(String sessionId);

    /**
     * 세션 ID와 상태로 요청 조회
     */
    List<HospitalRequest> findBySessionIdAndStatus(String sessionId, RequestStatus status);

    /**
     * 병원 ID로 요청 조회
     */
    List<HospitalRequest> findByHospitalId(Long hospitalId);

    /**
     * 세션 ID와 병원 ID로 PENDING 상태의 요청 조회 (병원 응답용)
     */
    Optional<HospitalRequest> findBySessionIdAndHospitalIdAndStatus(String sessionId, Long hospitalId, RequestStatus status);
}
