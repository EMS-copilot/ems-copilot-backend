package com.ems.copilot.emscopilot.repository;

import com.ems.copilot.emscopilot.domain.Hospital;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    /**
     * Vertex AI에서 사용하는 외부 ID로 병원 찾기
     * @param externalId "chh_001", "chh_002", "chh_003" 등
     */
    Optional<Hospital> findByExternalId(String externalId);

    List<Hospital> findByDistanceLessThanEqualOrderByDistanceAsc(Double distance);
}
