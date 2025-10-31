package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.PatientVitalData;
import com.ems.copilot.emscopilot.repository.PatientVitalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 환자 바이탈 정보를 Redis에 임시 저장하는 서비스
 * DB에는 저장하지 않음 (개인정보 보호)
 *
 * 삭제 조건:
 * 1. 3일 경과 시 자동 삭제 (Redis TTL)
 * 2. 병원 처치 완료 시 수동 삭제
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SessionStorageService {

    private final PatientVitalRepository vitalRepository;

    /**
     * 바이탈 정보 저장 (Redis에만, DB 저장 안 함)
     *
     * @param sessionId 세션 ID
     * @param vitalData 환자 바이탈 정보
     */
    public void saveVitalData(String sessionId, PatientVitalData vitalData) {
        vitalData.setSessionId(sessionId);

        vitalRepository.save(vitalData);
        log.info("바이탈 정보 Redis 저장 (DB 저장 안 함, TTL: 3일) - 세션 ID: {}, 현재 저장 수: {}",
                sessionId, vitalRepository.count());
    }

    /**
     * 바이탈 정보 조회
     * Redis TTL로 자동 만료되므로 별도 만료 체크 불필요
     *
     * @param sessionId 세션 ID
     * @return 바이탈 정보 (없으면 Empty)
     */
    public Optional<PatientVitalData> getVitalData(String sessionId) {
        Optional<PatientVitalData> data = vitalRepository.findById(sessionId);

        if (data.isEmpty()) {
            log.debug("바이탈 정보 없음 (만료되었거나 없음) - 세션 ID: {}", sessionId);
        }

        return data;
    }

    /**
     * 바이탈 정보 삭제
     * Encounter 확정, 세션 취소 등 상태 변경 시 호출
     *
     * @param sessionId 세션 ID
     */
    public void deleteVitalData(String sessionId) {
        if (vitalRepository.existsById(sessionId)) {
            vitalRepository.deleteById(sessionId);
            log.info("바이탈 정보 Redis에서 삭제 - 세션 ID: {}, 남은 수: {}",
                    sessionId, vitalRepository.count());
        } else {
            log.debug("삭제할 바이탈 정보 없음 - 세션 ID: {}", sessionId);
        }
    }

    /**
     * Redis TTL이 자동으로 만료 처리하므로
     * 별도 스케줄러 불필요
     * (기존 @Scheduled 메서드 제거)
     */

    /**
     * 현재 저장된 세션 수 조회
     *
     * @return 활성 세션 수
     */
    public long getActiveSessionCount() {
        return vitalRepository.count();
    }

    /**
     * 모든 세션 정리 (테스트/관리용)
     * 주의: 프로덕션에서는 신중하게 사용
     */
    public void clearAll() {
        long count = vitalRepository.count();
        vitalRepository.deleteAll();
        log.warn("모든 바이탈 정보 강제 삭제 - {}개", count);
    }

    /**
     * 구급대원 메모 저장/수정
     *
     * @param sessionCode 세션 코드
     * @param memo 메모 내용
     */
    public void updateMemo(String sessionCode, String memo) {
        Optional<PatientVitalData> vitalDataOpt = vitalRepository.findById(sessionCode);

        if (vitalDataOpt.isEmpty()) {
            log.error("바이탈 정보 없음 - 세션 코드: {}", sessionCode);
            throw new com.ems.copilot.emscopilot.exception.CustomException(
                    com.ems.copilot.emscopilot.exception.ErrorCode.VITAL_DATA_NOT_FOUND);
        }

        PatientVitalData vitalData = vitalDataOpt.get();
        vitalData.setParamedicMemo(memo);
        vitalRepository.save(vitalData);

        log.info("구급대원 메모 저장 완료 - 세션 코드: {}, 메모 길이: {}자", sessionCode, memo.length());
    }
}
