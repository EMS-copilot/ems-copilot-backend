package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.Hospital;
import com.ems.copilot.emscopilot.domain.PatientVitalData;
import com.ems.copilot.emscopilot.domain.SessionStatus;
import com.ems.copilot.emscopilot.domain.TransferSession;
import com.ems.copilot.emscopilot.dto.request.PatientDataRequest;
import com.ems.copilot.emscopilot.dto.request.VertexAIRequest;
import com.ems.copilot.emscopilot.dto.response.PatientRegistrationResponse;
import com.ems.copilot.emscopilot.dto.response.VertexAIResponse;
import com.ems.copilot.emscopilot.repository.HospitalRepository;
import com.ems.copilot.emscopilot.repository.TransferSessionRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * 환자 등록 시 세션 등록하는 로직 구현
 */

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final TransferSessionRepository sessionRepository;
    private final VertexAIService vertexAIService;
    private final HospitalRepository hospitalRepository;
    private final LocationService locationService;
    private final SessionStorageService storageService;
    private final com.ems.copilot.emscopilot.repository.UserRepository userRepository;

    @Transactional
    public PatientRegistrationResponse registerPatient(PatientDataRequest request, String employeeNumber) {
        // 1. 구급대원 조회
        com.ems.copilot.emscopilot.domain.User paramedic = userRepository.findByEmployeeNumber(employeeNumber)
                .orElseThrow(() -> new com.ems.copilot.emscopilot.exception.CustomException(
                        com.ems.copilot.emscopilot.exception.ErrorCode.USER_NOT_FOUND));
        log.info("구급대원 조회 완료 - 이름: {}, 사번: {}", paramedic.getName(), paramedic.getEmployeeNumber());

        // 2. 세션 코드 생성
        String sessionCode = generateSessionCode();
        log.info("세션 코드: {}", sessionCode);

        // 3. 환자 코드 생성
        String patientCode = generatePatientCode();
        log.info("환자 코드: {}", patientCode);

        // 4. 환자 임시 ID 생성 (UUID)
        String patientTempId = generatePatientTempId();
        log.info("환자 임시 ID: {}", patientTempId);

        String symptomString = String.join(", ", request.getSymptoms());

        // 4. 현재 위치 조회
        LocationService.CurrentLocation currentLocation = locationService.getCurrentLocation();
        log.info("현재 위치: {} ({}, {})",
                currentLocation.getAddress(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude());

        // 5. TransferSession 생성 및 저장
        TransferSession session = TransferSession.builder()
                .sessionCode(sessionCode)
                .patientCode(patientCode)
                .patientTempId(patientTempId)
                .paramedic(paramedic)
                .status(SessionStatus.PENDING)
                .currentAddress(currentLocation.getAddress())
                .currentLatitude(currentLocation.getLatitude())
                .currentLongitude(currentLocation.getLongitude())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        // 주증상 설정 (Builder 후)
        session.setChiefComplaintList(request.getSymptoms());

        session = sessionRepository.save(session);
        log.info("TransferSession DB 저장 완료 (바이탈 정보 제외) - ID: {}", session.getId());

        // 5-1. 바이탈 정보는 메모리에만 저장 (DB 저장 안 함)
        PatientVitalData vitalData = PatientVitalData.builder()
                .sessionId(session.getSessionCode())
                .age(request.getAge())
                .sex(request.getSex())
                .triageLevel(request.getTriageLevel())
                .sbp(request.getSbp())
                .dbp(request.getDbp())
                .hr(request.getHr())
                .rr(request.getRr())
                .spo2(request.getSpo2())
                .temp(request.getTemp())
                .symptoms(request.getSymptoms())
                .build();

        storageService.saveVitalData(session.getSessionCode(), vitalData);
        log.info("바이탈 정보 메모리 저장 완료 (DB 저장 안 함)");

        // 6. 전체 병원 조회 (Vertex AI가 필터링함)
        List<Hospital> candidateHospitals = hospitalRepository.findAll();
        log.info("전체 후보 병원 {}개 조회", candidateHospitals.size());

        // 7. Vertex AI 요청 객체 생성
        VertexAIRequest aiRequest = buildVertexAIRequest(
                patientTempId,
                request,
                candidateHospitals,
                symptomString
        );

        // 8. Vertex AI 호출
        VertexAIResponse aiResponse = vertexAIService.analyzePatient(aiRequest);
        log.info("Vertex AI 응답 받음 - 추천 병원 {}개", aiResponse.getPredictions().size());

        // 9. AI응답을 백엔드 형식으로 반환
        List<PatientRegistrationResponse.RecommendedHospital> hospitals =
                convertToRecommendedHospitals(aiResponse);

        return PatientRegistrationResponse.builder()
                .sessionCode(session.getSessionCode())
                .patientCode(patientCode)
                .patientTempId(patientTempId)
                .recommendedHospitals(hospitals)
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .status(String.valueOf(session.getStatus()))
                .build();
    }

    /**
     * Vertex ai 응답 -> 추천 병원 리스트로 변환
     */
    private List<PatientRegistrationResponse.RecommendedHospital> convertToRecommendedHospitals(
            VertexAIResponse aiResponse) {
        List<PatientRegistrationResponse.RecommendedHospital> result = new ArrayList<>();
        int priority = 1;

        for (VertexAIResponse.Prediction prediction : aiResponse.getPredictions()) {
            // AI의 hospital_id로 DB에서 병원 찾기
            Optional<Hospital> hospitalOpt = hospitalRepository.findByExternalId(prediction.getHospitalId());

            if (hospitalOpt.isEmpty()) {
                log.warn("병원 없음: {}", prediction.getHospitalId());
                continue;
            }

            Hospital hospital = hospitalOpt.get();

            Double distance = hospital.getDistance();

            Integer eta = hospital.getEta();

            // 추천 병원 객체 생성
            PatientRegistrationResponse.RecommendedHospital recommendedHospital =
                    PatientRegistrationResponse.RecommendedHospital.builder()
                            .hospitalId(hospital.getId())
                            .hospitalName(hospital.getName())
                            .aiScore(prediction.getScoreValue())
                            .priority(priority++)
                            .aiExplanations(prediction.getExplanations())
                            .distance(distance)
                            .eta(eta)
                            .build();

            result.add(recommendedHospital);
        }
        log.info("변환 완료 - 병원 {}개", result.size());
        return result;
    }

    /**
     * Vertex AI 요청 객체 생성
     */
    private VertexAIRequest buildVertexAIRequest(
            String patientTempId,
            PatientDataRequest request,
            List<Hospital> candidateHospitals,
            String symptomString) {

        // Patient 정보 구성
        VertexAIRequest.Patient patient = VertexAIRequest.Patient.builder()
                .id(patientTempId)
                .age(request.getAge())
                .sex(request.getSex())
                .triageLevel(request.getTriageLevel())
                .symptom(symptomString)
                .bpSystolic(request.getSbp())
                .hr(request.getHr())
                .build();

        // 후보 병원 리스트 구성
        List<VertexAIRequest.CandidateHospital> candidates = candidateHospitals.stream()
                .map(hospital -> VertexAIRequest.CandidateHospital.builder()
                        .hospitalId(hospital.getExternalId())
                        .hospitalCapacity(hospital.getHospitalCapacity())
                        .icuBeds(hospital.getIcuBeds())
                        .erBeds(hospital.getErBeds())
                        .distanceKm(hospital.getDistance())
                        .etaMinutes(hospital.getEta())
                        .build())
                .toList();

        // result_method 구성
        VertexAIRequest.ResultMethod resultMethod = VertexAIRequest.ResultMethod.builder()
                .topK(5)
                .build();

        return VertexAIRequest.builder()
                .patient(patient)
                .candidateHospitals(candidates)
                .resultMethod(resultMethod)
                .build();
    }


    /**
     * 세션 코드 생성
     */
    private String generateSessionCode() {
        String year = String.valueOf(Year.now().getValue());
        Integer maxNum = sessionRepository.findMaxSessionNumberByYear(year);
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        return String.format("S%s-%03d", year, nextNum);
    }

    /**
     * 환자 코드 생성
     */
    private String generatePatientCode() {
        String year = String.valueOf(Year.now().getValue());
        Integer maxNum = sessionRepository.findMaxSessionNumberByYear(year);
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        return String.format("PT%s-%03d", year, nextNum);
    }

    /**
     * 환자 임시 ID 생성 (UUID)
     */
    private String generatePatientTempId() {
        return "patient_" + UUID.randomUUID().toString().replace("-", "");
    }
}