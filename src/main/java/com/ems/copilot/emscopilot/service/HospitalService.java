package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.Hospital;
import com.ems.copilot.emscopilot.domain.RequestStatus;
import com.ems.copilot.emscopilot.dto.response.HospitalResponse;
import com.ems.copilot.emscopilot.exception.CustomException;
import com.ems.copilot.emscopilot.exception.ErrorCode;
import com.ems.copilot.emscopilot.repository.HospitalRepository;
import com.ems.copilot.emscopilot.repository.HospitalRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class HospitalService {

    private final HospitalRepository hospitalRepository;
    private final HospitalRequestRepository hospitalRequestRepository;

    public List<HospitalResponse> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        return hospitals.stream()
                .map(HospitalResponse::response)
                .collect(Collectors.toList());
    }

    public HospitalResponse getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return HospitalResponse.response(hospital);
    }

    public List<HospitalResponse> getHospitalByDistance(Double maxDistance) {
        List<Hospital> hospitals = hospitalRepository.findByDistanceLessThanEqualOrderByDistanceAsc(maxDistance);
        return hospitals.stream()
                .map(HospitalResponse::response)
                .collect(Collectors.toList());
    }

    // ==================== 병원 랭킹 계산 ====================

    /**
     * 특정 병원의 랭킹 재계산
     *
     * 랭킹 산정 기준:
     * - 주간 누적 수용 인원 수 (지난 7일간 ACCEPTED 건수)
     * - 거절 50건마다 -10명 감점
     * - 최종 점수 = Math.max(0, 수용건수 - 감점)
     * - 등급: A(401~) / B(301~400) / C(201~300) / D(101~200) / E(1~100) / F(0)
     *
     * @param hospitalId 병원 ID
     */
    @Transactional
    public void calculateRank(Long hospitalId) {
        log.info("병원 랭킹 계산 시작 - 병원 ID: {}", hospitalId);

        Hospital hospital = hospitalRepository.findById(hospitalId)
                .orElseThrow(() -> new CustomException(ErrorCode.HOSPITAL_NOT_FOUND));

        // 지난 7일 시작 시점
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        // 수용 건수 (ACCEPTED)
        long acceptedCount = hospitalRequestRepository.countByHospitalIdAndStatusAndCreatedAtAfter(
                hospitalId, RequestStatus.ACCEPTED, sevenDaysAgo);

        // 거절 건수 (REJECTED)
        long rejectedCount = hospitalRequestRepository.countByHospitalIdAndStatusAndCreatedAtAfter(
                hospitalId, RequestStatus.REJECTED, sevenDaysAgo);

        // 감점 계산: 거절 50건마다 -10명
        long penalty = (rejectedCount / 50) * 10;

        // 최종 점수: 수용 - 감점 (최저 0)
        long finalScore = Math.max(0, acceptedCount - penalty);

        // 등급 산정
        Character rank = calculateGrade(finalScore);

        // 병원 랭크 업데이트
        hospital.updateRank(rank);
        hospitalRepository.save(hospital);

        log.info("병원 랭킹 계산 완료 - 병원: {}, 수용: {}, 거절: {}, 감점: {}, 최종점수: {}, 등급: {}",
                hospital.getName(), acceptedCount, rejectedCount, penalty, finalScore, rank);
    }

    /**
     * 특정 병원의 랭킹 재계산 (비동기)
     *
     * @param hospitalId 병원 ID
     */
    @Async
    public void calculateRankAsync(Long hospitalId) {
        try {
            calculateRank(hospitalId);
        } catch (Exception e) {
            log.error("병원 랭킹 계산 실패 - 병원 ID: {}", hospitalId, e);
        }
    }

    /**
     * 모든 병원의 랭킹 재계산
     */
    @Transactional
    public void recalculateAllRanks() {
        log.info("전체 병원 랭킹 재계산 시작");

        List<Hospital> hospitals = hospitalRepository.findAll();

        for (Hospital hospital : hospitals) {
            try {
                calculateRank(hospital.getId());
            } catch (Exception e) {
                log.error("병원 랭킹 계산 실패 - 병원 ID: {}", hospital.getId(), e);
            }
        }

        log.info("전체 병원 랭킹 재계산 완료 - 총 {}개 병원", hospitals.size());
    }

    /**
     * 점수에 따른 등급 산정
     *
     * @param score 최종 점수
     * @return 등급 (A~F)
     */
    private Character calculateGrade(long score) {
        if (score >= 401) {
            return 'A';
        } else if (score >= 301) {
            return 'B';
        } else if (score >= 201) {
            return 'C';
        } else if (score >= 101) {
            return 'D';
        } else if (score >= 1) {
            return 'E';
        } else {
            return 'F';
        }
    }
}
