package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.Hospital;
import com.ems.copilot.emscopilot.domain.TransferSession;
import com.ems.copilot.emscopilot.dto.request.PatientDataRequest;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientService {

    private final TransferSessionRepository sessionRepository;
    private final VertexAIService vertexAIService;
    private final HospitalRepository hospitalRepository;
    private final LocationService locationService;

    @Transactional
    public PatientRegistrationResponse registerPatient(PatientDataRequest request) {
        String sessionCode = generateSessionCode();
        log.info("세션 코드: {}", sessionCode);

        String patientTempId = generatePatientTempId();
        log.info("환자 임시 ID: {}", patientTempId);

        String symptomString = String.join(", ", request.getSymptoms());

        LocationService.CurrentLocation currentLocation = locationService.getCurrentLocation();
        log.info("현재 위치: {} ({}, {})",
                currentLocation.getAddress(),
                currentLocation.getLatitude(),
                currentLocation.getLongitude());

        VertexAIResponse aiResponse = vertexAIService.analyzePatient(
                patientTempId,
                request.getAge(),
                request.getSex(),
                request.getTriageLevel(),
                request.getSbp(),
                request.getHr(),
                symptomString
        );

        log.info("Vertex AI 응답 받음 - 추천 병원 {}개", aiResponse.getPredictions().size());

        TransferSession session = TransferSession.builder()
                .sessionCode(sessionCode)
                .status("PENDING")
                .chiefComplaint(request.getSymptoms())
                .currentAddress(currentLocation.getAddress())
                .currentLatitude(currentLocation.getLatitude())
                .currentLongitude(currentLocation.getLongitude())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .build();

        session = sessionRepository.save(session);
        log.info("세션 저장 완료 - ID: {}", session.getId());

        // AI응답을 우리 형식으로 반환
        List<PatientRegistrationResponse.RecommendedHospital> hospitals =
                convertToRecommendedHospitals(aiResponse);

        return PatientRegistrationResponse.builder()
                .sessionId(session.getId())
                .sessionCode(session.getSessionCode())
                .patientTempId(patientTempId)
                .recommendedHospitals(hospitals)
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .status(session.getStatus())
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
                            .hospitalId(prediction.getHospitalId())
                            .hospitalName(hospital.getName())
                            .aiScore(prediction.getScore())
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
     * 세션 코드 생성
     */
    private String generateSessionCode() {
        String year = String.valueOf(Year.now().getValue());
        Integer maxNum = sessionRepository.findMaxSessionNumberByYear(year);
        int nextNum = (maxNum != null ? maxNum : 0) + 1;
        return String.format("P%s-%03d", year, nextNum);
    }

    /**
     * 환자 임시 ID 생성
     */
    private String generatePatientTempId() {
        return "patient_" + UUID.randomUUID().toString().replace("-", "");
    }
}