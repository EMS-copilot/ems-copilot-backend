package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.*;
import com.ems.copilot.emscopilot.dto.request.ConfirmEncounterRequest;
import com.ems.copilot.emscopilot.dto.response.EncounterResponse;
import com.ems.copilot.emscopilot.exception.CustomException;
import com.ems.copilot.emscopilot.exception.ErrorCode;
import com.ems.copilot.emscopilot.repository.EncounterRepository;
import com.ems.copilot.emscopilot.repository.HospitalRequestRepository;
import com.ems.copilot.emscopilot.repository.TransferSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Encounter 관리 서비스
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EncounterService {

    private final EncounterRepository encounterRepository;
    private final HospitalRequestRepository hospitalRequestRepository;
    private final TransferSessionRepository transferSessionRepository;
    private final SessionStorageService storageService;
    private final com.ems.copilot.emscopilot.repository.UserRepository userRepository;

    /**
     * 최종 병원 확정 (Encounter 생성)
     *
     * @param request 병원 요청 ID
     * @return 생성된 Encounter (바이탈 정보는 별도로 조회 필요)
     */
    @Transactional
    public Encounter confirmEncounter(ConfirmEncounterRequest request) {
        Long hospitalRequestId = request.getHospitalRequestId();

        log.info("Encounter 확정 시작 - 병원 요청 ID: {}", hospitalRequestId);

        // 1. HospitalRequest 조회
        HospitalRequest hospitalRequest = hospitalRequestRepository.findById(hospitalRequestId)
                .orElseThrow(() -> new CustomException(ErrorCode.HOSPITAL_REQUEST_NOT_FOUND));

        // 2. ACCEPTED 상태 확인
        if (hospitalRequest.getStatus() != RequestStatus.ACCEPTED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_STATUS);
        }

        // 3. 만료 체크
        if (hospitalRequest.isExpired()) {
            hospitalRequest.setStatus(RequestStatus.EXPIRED);
            hospitalRequestRepository.save(hospitalRequest);
            throw new CustomException(ErrorCode.HOSPITAL_REQUEST_EXPIRED);
        }

        String sessionCode = hospitalRequest.getSessionCode();

        // 4. Redis에서 바이탈 정보 조회 (확인용 - DB에는 저장하지 않음)
        PatientVitalData vitalData = storageService.getVitalData(sessionCode)
                .orElseThrow(() -> new CustomException(ErrorCode.VITAL_DATA_NOT_FOUND));

        log.info("바이탈 정보 조회 성공 (DB 저장 안 함, Redis에만 유지) - age: {}, sex: {}, triageLevel: {}",
                vitalData.getAge(), vitalData.getSex(), vitalData.getTriageLevel());

        // 5. TransferSession 조회 (환자 코드, 세션 코드 가져오기)
        TransferSession session = transferSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        // 6. Encounter 생성 (개인정보 최소화 - 바이탈은 DB에 저장하지 않음)
        Encounter encounter = Encounter.builder()
                .patientCode(session.getPatientCode())
                .patientTempId(session.getPatientTempId())
                .sessionCode(session.getSessionCode())
                .paramedic(session.getParamedic())
                .hospital(hospitalRequest.getHospital())
                // 기본 정보만 저장 (개인정보 최소화)
                .age(vitalData.getAge())
                .sex(vitalData.getSex())
                .triageLevel(vitalData.getTriageLevel())
                // 바이탈 사인은 개인정보 보호를 위해 DB에 저장하지 않음!
                // Redis에서만 임시 사용, 병원 완료 시 삭제
                // 이송 정보
                .transferLocation(session.getCurrentAddress())
                .transferDistance(0.0) // TODO: 실제 거리 계산
                .transferEta(0) // TODO: 실제 시간 계산
                // AI 정보
                .aiScore(0.0) // TODO: AI 점수
                .aiPriority(0) // TODO: AI 우선순위
                // 상태
                .status(SessionStatus.IN_TRANSIT)
                .build();

        Encounter savedEncounter = encounterRepository.save(encounter);

        log.info("Encounter 생성 완료 - ID: {}, 환자 코드: {}, 병원: {}",
                savedEncounter.getId(),
                savedEncounter.getPatientCode(),
                savedEncounter.getHospital().getName());

        // 7. TransferSession 상태 업데이트 (IN_TRANSIT로 동기화)
        session.setStatus(SessionStatus.IN_TRANSIT);
        transferSessionRepository.save(session);
        log.info("TransferSession 상태 업데이트 - 세션: {}, 상태: IN_TRANSIT", sessionCode);

        // 8. Redis 바이탈은 아직 유지 (병원 완료 시 삭제)
        // 바이탈 정보는 개인정보 보호를 위해 DB에 저장하지 않고 Redis에만 유지
        // 병원에서 처치 완료 시 Redis에서 삭제됨
        log.info("Redis 바이탈 정보 유지 (DB 저장 안 함) - 병원 완료 시 삭제됨");

        return savedEncounter;
    }

    /**
     * 병원 도착 처리 (이송 완료)
     *
     * @param encounterId Encounter ID
     * @return 업데이트된 Encounter
     */
    @Transactional
    public Encounter arriveAtHospital(Long encounterId) {
        log.info("병원 도착 처리 시작 - ID: {}", encounterId);

        // 1. Encounter 조회
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENCOUNTER_NOT_FOUND));

        // 2. 상태 확인 (IN_TRANSIT 상태여야 함)
        if (encounter.getStatus() != SessionStatus.IN_TRANSIT) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_STATUS);
        }

        // 3. 상태 업데이트
        encounter.setStatus(SessionStatus.ARRIVED);
        encounter.setTransferredAt(java.time.LocalDateTime.now());

        Encounter updatedEncounter = encounterRepository.save(encounter);

        log.info("병원 도착 처리 완료 - ID: {}", updatedEncounter.getId());

        // 4. TransferSession 상태 업데이트 (ARRIVED로 동기화)
        String sessionCode = encounter.getSessionCode();
        TransferSession session = transferSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        session.setStatus(SessionStatus.ARRIVED);
        transferSessionRepository.save(session);
        log.info("TransferSession 상태 업데이트 - 세션: {}, 상태: ARRIVED", sessionCode);

        return updatedEncounter;
    }

    /**
     * 병원 완료 처리 (처치 종료)
     *
     * @param encounterId Encounter ID
     * @return 업데이트된 Encounter
     */
    @Transactional
    public Encounter completeEncounter(Long encounterId) {
        log.info("Encounter 완료 처리 시작 - ID: {}", encounterId);

        // 1. Encounter 조회
        Encounter encounter = encounterRepository.findById(encounterId)
                .orElseThrow(() -> new CustomException(ErrorCode.ENCOUNTER_NOT_FOUND));

        // 2. 상태 확인 (ARRIVED 상태여야 함)
        if (encounter.getStatus() == SessionStatus.COMPLETED) {
            throw new CustomException(ErrorCode.ENCOUNTER_ALREADY_COMPLETED);
        }
        if (encounter.getStatus() != SessionStatus.ARRIVED) {
            throw new CustomException(ErrorCode.INVALID_REQUEST_STATUS);
        }

        // 3. 상태 업데이트
        encounter.setStatus(SessionStatus.COMPLETED);
        encounter.setCompletedAt(java.time.LocalDateTime.now());

        Encounter updatedEncounter = encounterRepository.save(encounter);

        log.info("Encounter 완료 처리 성공 - ID: {}", updatedEncounter.getId());

        // 4. TransferSession 상태 업데이트 (COMPLETED로 동기화)
        String sessionCode = encounter.getSessionCode();
        TransferSession session = transferSessionRepository.findBySessionCode(sessionCode)
                .orElseThrow(() -> new CustomException(ErrorCode.SESSION_NOT_FOUND));

        session.setStatus(SessionStatus.COMPLETED);
        transferSessionRepository.save(session);
        log.info("TransferSession 상태 업데이트 - 세션: {}, 상태: COMPLETED", sessionCode);

        // 5. Redis 바이탈 삭제
        storageService.deleteVitalData(sessionCode);

        log.info("Redis 바이탈 정보 삭제 완료 - 세션 코드: {}", sessionCode);

        return updatedEncounter;
    }

    /**
     * 구급대원이 담당한 모든 이송 내역 조회 (최신순)
     *
     * @param employeeNumber 구급대원 사번
     * @return EncounterResponse 목록 (증상 정보 포함)
     */
    @Transactional(readOnly = true)
    public List<EncounterResponse> getMyEncounters(String employeeNumber) {
        log.info("구급대원 이송 내역 조회 시작 - 사번: {}", employeeNumber);

        User paramedic = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Encounter> encounters = encounterRepository.findByParamedicIdOrderByCreatedAtDesc(paramedic.getId());

        log.info("구급대원 이송 내역 조회 완료 - 총 {}개", encounters.size());

        // Entity -> DTO 변환 (증상 정보 포함)
        return encounters.stream()
                .map(encounter -> {
                    // TransferSession에서 증상 정보 조회
                    TransferSession session = transferSessionRepository.findBySessionCode(encounter.getSessionCode())
                            .orElse(null);

                    if (session != null) {
                        return EncounterResponse.fromWithSession(encounter, session.getChiefComplaintList());
                    } else {
                        return EncounterResponse.from(encounter);
                    }
                })
                .toList();
    }

    /**
     * 구급대원이 담당한 이송 내역 중 특정 상태 조회
     *
     * @param employeeNumber 구급대원 사번
     * @param status 상태
     * @return EncounterResponse 목록 (증상 정보 포함)
     */
    @Transactional(readOnly = true)
    public List<EncounterResponse> getMyEncountersByStatus(String employeeNumber, SessionStatus status) {
        log.info("구급대원 이송 내역 조회 시작 - 사번: {}, 상태: {}", employeeNumber, status);

        User paramedic = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        List<Encounter> encounters = encounterRepository.findByParamedicIdAndStatus(paramedic.getId(), status);

        log.info("구급대원 이송 내역 조회 완료 - 상태: {}, 총 {}개", status, encounters.size());

        // Entity -> DTO 변환 (증상 정보 포함)
        return encounters.stream()
                .map(encounter -> {
                    // TransferSession에서 증상 정보 조회
                    TransferSession session = transferSessionRepository.findBySessionCode(encounter.getSessionCode())
                            .orElse(null);

                    if (session != null) {
                        return EncounterResponse.fromWithSession(encounter, session.getChiefComplaintList());
                    } else {
                        return EncounterResponse.from(encounter);
                    }
                })
                .toList();
    }

    /**
     * 병원이 받은 모든 이송 내역 조회 (최신순)
     *
     * @param employeeNumber 병원 직원 사번
     * @return EncounterResponse 목록 (증상 정보 포함)
     */
    @Transactional(readOnly = true)
    public List<EncounterResponse> getHospitalEncounters(String employeeNumber) {
        log.info("병원 이송 내역 조회 시작 - 사번: {}", employeeNumber);

        User hospitalStaff = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        Hospital hospital = hospitalStaff.getHospital();
        if (hospital == null) {
            throw new CustomException(ErrorCode.USER_HAS_NO_HOSPITAL);
        }

        List<Encounter> encounters = encounterRepository.findByHospitalIdOrderByCreatedAtDesc(hospital.getId());

        log.info("병원 이송 내역 조회 완료 - 병원: {}, 총 {}개", hospital.getName(), encounters.size());

        // Entity -> DTO 변환 (증상 정보 포함)
        return encounters.stream()
                .map(encounter -> {
                    // TransferSession에서 증상 정보 조회
                    TransferSession session = transferSessionRepository.findBySessionCode(encounter.getSessionCode())
                            .orElse(null);

                    if (session != null) {
                        return EncounterResponse.fromWithSession(encounter, session.getChiefComplaintList());
                    } else {
                        return EncounterResponse.from(encounter);
                    }
                })
                .toList();
    }
}
