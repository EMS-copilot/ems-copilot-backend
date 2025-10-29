package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.*;
import com.ems.copilot.emscopilot.dto.request.HospitalResponseRequest;
import com.ems.copilot.emscopilot.dto.request.SendToHospitalsRequest;
import com.ems.copilot.emscopilot.dto.response.SendToHospitalsResponse;
import com.ems.copilot.emscopilot.exception.CustomException;
import com.ems.copilot.emscopilot.exception.ErrorCode;
import com.ems.copilot.emscopilot.repository.HospitalRepository;
import com.ems.copilot.emscopilot.repository.HospitalRequestRepository;
import com.ems.copilot.emscopilot.repository.TransferSessionRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 병원 요청 관리 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TransferRequestService {

    private final HospitalRequestRepository requestRepository;
    private final HospitalRepository hospitalRepository;
    private final TransferSessionRepository sessionRepository;
    private final SessionStorageService storageService;
    private final ObjectMapper objectMapper;
    private final com.ems.copilot.emscopilot.repository.UserRepository userRepository;

    /**
     * 선택한 병원들에게 환자 정보 전송
     *
     * @param request 세션 코드와 선택한 병원 ID 목록
     * @param employeeNumber 현재 로그인한 구급대원 사번
     * @return 전송 결과
     */
    @Transactional
    public SendToHospitalsResponse sendToHospitals(SendToHospitalsRequest request, String employeeNumber) {
        String sessionCode = request.getSessionCode();
        List<Long> hospitalIds = request.getHospitalIds();

        log.info("병원 전송 시작 - 세션 코드: {}, 병원 수: {}", sessionCode, hospitalIds.size());

        // 1. 구급대원 조회
        User paramedic = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        log.info("구급대원 조회 완료 - 이름: {}, 사번: {}", paramedic.getName(), paramedic.getEmployeeNumber());

        // 2. TransferSession 조회 (환자 코드 가져오기)
        TransferSession session = sessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));
        log.info("세션 조회 완료 - 환자 코드: {}", session.getPatientCode());

        // 3. Redis에서 바이탈 정보 조회
        PatientVitalData vitalData = storageService.getVitalData(sessionCode)
                .orElseThrow(() -> new CustomException(ErrorCode.VITAL_DATA_NOT_FOUND));

        log.info("바이탈 정보 조회 성공 - 나이: {}, 성별: {}, KTAS: {}",
                vitalData.getAge(), vitalData.getSex(), vitalData.getTriageLevel());

        // 4. 각 병원별 HospitalRequest 생성
        List<SendToHospitalsResponse.HospitalRequestInfo> results = new ArrayList<>();

        for (Long hospitalId : hospitalIds) {
            // 병원 조회
            Hospital hospital = hospitalRepository.findById(hospitalId)
                    .orElseThrow(() -> new CustomException(ErrorCode.HOSPITAL_NOT_FOUND));

            // 증상 리스트를 JSON 문자열로 변환
            String symptomsJson = null;
            try {
                if (vitalData.getSymptoms() != null && !vitalData.getSymptoms().isEmpty()) {
                    symptomsJson = objectMapper.writeValueAsString(vitalData.getSymptoms());
                }
            } catch (JsonProcessingException e) {
                log.error("증상 데이터 JSON 변환 실패", e);
            }

            // HospitalRequest 생성
            HospitalRequest hospitalRequest = HospitalRequest.builder()
                    .sessionCode(sessionCode)
                    .patientCode(session.getPatientCode())
                    .paramedic(paramedic)
                    .hospital(hospital)
                    .age(vitalData.getAge())
                    .sex(vitalData.getSex())
                    .triageLevel(vitalData.getTriageLevel())
                    .symptoms(symptomsJson)
                    .status(RequestStatus.PENDING)
                    .expiresAt(LocalDateTime.now().plusMinutes(30))
                    .build();

            // DB 저장
            HospitalRequest savedRequest = requestRepository.save(hospitalRequest);

            log.info("병원 요청 생성 - 요청 ID: {}, 병원: {}", savedRequest.getId(), hospital.getName());

            // 결과 추가
            results.add(SendToHospitalsResponse.HospitalRequestInfo.builder()
                    .requestId(savedRequest.getId())
                    .hospitalId(hospital.getId())
                    .hospitalName(hospital.getName())
                    .status(RequestStatus.PENDING.name())
                    .build());
        }

        log.info("병원 전송 완료 - 총 {}개 병원", results.size());

        // 바이탈 정보 DTO 생성 (프론트엔드 확인용)
        SendToHospitalsResponse.PatientVitalInfo vitalInfo = SendToHospitalsResponse.PatientVitalInfo.builder()
                .age(vitalData.getAge())
                .sex(vitalData.getSex())
                .triageLevel(vitalData.getTriageLevel())
                .sbp(vitalData.getSbp())
                .dbp(vitalData.getDbp())
                .hr(vitalData.getHr())
                .rr(vitalData.getRr())
                .spo2(vitalData.getSpo2())
                .temp(vitalData.getTemp())
                .symptoms(vitalData.getSymptoms())
                .build();

        return SendToHospitalsResponse.builder()
                .sessionCode(sessionCode)
                .totalSent(results.size())
                .patientVital(vitalInfo)
                .requests(results)
                .build();
    }

    /**
     * 병원 응답 처리 (수용/거절) - 세션 기반
     *
     * @param sessionCode 세션 코드
     * @param hospitalId 병원 ID (현재 로그인한 병원)
     * @param responseRequest 응답 (ACCEPTED/REJECTED)
     * @return 업데이트된 HospitalRequest
     */
    @Transactional
    public HospitalRequest respondToRequest(String sessionCode, Long hospitalId, HospitalResponseRequest responseRequest) {
        log.info("병원 응답 처리 시작 - 세션 코드: {}, 병원 ID: {}, 응답: {}",
                sessionCode, hospitalId, responseRequest.getResponse());

        // 1. 세션 코드와 병원 ID로 PENDING 상태의 HospitalRequest 조회
        HospitalRequest hospitalRequest = requestRepository.findBySessionCodeAndHospitalIdAndStatus(
                sessionCode, hospitalId, RequestStatus.PENDING)
                .orElseThrow(() -> new CustomException(ErrorCode.NO_PENDING_REQUEST));

        // 2. 만료 체크
        if (hospitalRequest.isExpired()) {
            hospitalRequest.setStatus(RequestStatus.EXPIRED);
            requestRepository.save(hospitalRequest);
            throw new CustomException(ErrorCode.HOSPITAL_REQUEST_EXPIRED);
        }

        // 3. 이미 응답했는지 체크
        if (hospitalRequest.getStatus() != RequestStatus.PENDING) {
            throw new CustomException(ErrorCode.ALREADY_RESPONDED);
        }

        // 4. 응답 처리
        RequestStatus newStatus = RequestStatus.valueOf(responseRequest.getResponse());
        hospitalRequest.respond(newStatus, responseRequest.getMessage());

        HospitalRequest savedRequest = requestRepository.save(hospitalRequest);

        log.info("병원 응답 처리 완료 - 요청 ID: {}, 병원: {}, 상태: {}",
                savedRequest.getId(),
                savedRequest.getHospital().getName(),
                savedRequest.getStatus());

        return savedRequest;
    }

    /**
     * 세션 코드로 모든 병원 요청 조회
     *
     * @param sessionCode 세션 코드
     * @return 병원 요청 목록
     */
    @Transactional(readOnly = true)
    public List<HospitalRequest> getRequestsBySessionCode(String sessionCode) {
        log.info("세션 코드로 병원 요청 목록 조회 - 세션 코드: {}", sessionCode);

        List<HospitalRequest> requests = requestRepository.findBySessionCode(sessionCode);

        log.info("병원 요청 목록 조회 완료 - 총 {}개", requests.size());

        return requests;
    }

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

    /**
     * 병원이 받은 모든 요청 조회 (최신순)
     *
     * @param employeeNumber 병원 직원 사번
     * @return 병원 요청 목록
     */
    @Transactional(readOnly = true)
    public List<HospitalRequest> getHospitalRequests(String employeeNumber) {
        log.info("병원 요청 목록 조회 시작 - 사번: {}", employeeNumber);

        User hospitalStaff = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Hospital hospital = hospitalStaff.getHospital();
        if (hospital == null) {
            throw new CustomException(ErrorCode.USER_HAS_NO_HOSPITAL);
        }

        List<HospitalRequest> requests = requestRepository.findByHospitalId(hospital.getId());

        log.info("병원 요청 목록 조회 완료 - 병원: {}, 총 {}개", hospital.getName(), requests.size());

        return requests;
    }

    /**
     * 병원이 받은 요청 중 특정 상태의 요청 조회
     *
     * @param employeeNumber 병원 직원 사번
     * @param status 요청 상태
     * @return 병원 요청 목록
     */
    @Transactional(readOnly = true)
    public List<HospitalRequest> getHospitalRequestsByStatus(String employeeNumber, RequestStatus status) {
        log.info("병원 요청 목록 조회 시작 - 사번: {}, 상태: {}", employeeNumber, status);

        User hospitalStaff = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Hospital hospital = hospitalStaff.getHospital();
        if (hospital == null) {
            throw new CustomException(ErrorCode.USER_HAS_NO_HOSPITAL);
        }

        List<HospitalRequest> requests = requestRepository.findByHospitalIdAndStatus(hospital.getId(), status);

        log.info("병원 요청 목록 조회 완료 - 병원: {}, 상태: {}, 총 {}개", hospital.getName(), status, requests.size());

        return requests;
    }
}
