package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.SessionStatus;
import com.ems.copilot.emscopilot.domain.TransferSession;
import com.ems.copilot.emscopilot.domain.User;
import com.ems.copilot.emscopilot.exception.CustomException;
import com.ems.copilot.emscopilot.exception.ErrorCode;
import com.ems.copilot.emscopilot.repository.TransferSessionRepository;
import com.ems.copilot.emscopilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * TransferSession 조회 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransferSessionService {

    private final TransferSessionRepository sessionRepository;
    private final UserRepository userRepository;

    /**
     * 세션 코드로 세션 정보 조회
     *
     * @param sessionCode 세션 코드
     * @return 세션 정보
     */
    @Transactional(readOnly = true)
    public TransferSession getSessionByCode(String sessionCode) {
        log.info("세션 코드로 세션 조회 - 세션 코드: {}", sessionCode);

        TransferSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        log.info("세션 조회 완료 - 환자 코드: {}, 상태: {}", session.getPatientCode(), session.getStatus());

        return session;
    }

    /**
     * 구급대원이 생성한 모든 세션 조회 (최신순)
     *
     * @param employeeNumber 구급대원 사번
     * @return 세션 목록
     */
    @Transactional(readOnly = true)
    public List<TransferSession> getMyTransferSessions(String employeeNumber) {
        log.info("구급대원 세션 목록 조회 시작 - 사번: {}", employeeNumber);

        User paramedic = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<TransferSession> sessions = sessionRepository.findByParamedicIdOrderByCreatedAtDesc(paramedic.getId());

        log.info("구급대원 세션 목록 조회 완료 - 총 {}개", sessions.size());

        return sessions;
    }

    /**
     * 구급대원이 생성한 세션 중 특정 상태의 세션 조회
     *
     * @param employeeNumber 구급대원 사번
     * @param status 세션 상태
     * @return 세션 목록
     */
    @Transactional(readOnly = true)
    public List<TransferSession> getMyTransferSessionsByStatus(String employeeNumber, SessionStatus status) {
        log.info("구급대원 세션 목록 조회 시작 - 사번: {}, 상태: {}", employeeNumber, status);

        User paramedic = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<TransferSession> sessions = sessionRepository.findByParamedicIdAndStatus(paramedic.getId(), status);

        log.info("구급대원 세션 목록 조회 완료 - 상태: {}, 총 {}개", status, sessions.size());

        return sessions;
    }
}
