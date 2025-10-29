package com.ems.copilot.emscopilot.repository;

import com.ems.copilot.emscopilot.domain.Encounter;
import com.ems.copilot.emscopilot.domain.EncounterStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EncounterRepository extends JpaRepository<Encounter, Long> {

    /**
     * 세션 코드로 Encounter 조회
     */
    Optional<Encounter> findBySessionCode(String sessionCode);

    /**
     * 환자 코드로 Encounter 조회
     */
    Optional<Encounter> findByPatientCode(String patientCode);

    /**
     * 병원 ID로 Encounter 목록 조회
     */
    List<Encounter> findByHospitalId(Long hospitalId);

    /**
     * 상태로 Encounter 목록 조회
     */
    List<Encounter> findByStatus(EncounterStatus status);

    /**
     * 구급대원 ID로 이송 내역 조회 (최신순)
     */
    List<Encounter> findByParamedicIdOrderByCreatedAtDesc(Long paramedicId);

    /**
     * 구급대원 ID와 상태로 이송 내역 조회
     */
    List<Encounter> findByParamedicIdAndStatus(Long paramedicId, EncounterStatus status);
}
