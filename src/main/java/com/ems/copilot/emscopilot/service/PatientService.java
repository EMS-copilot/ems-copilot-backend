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
                convertToRecommendedHospitals(aiResponse,
                        currentLocation.getLongitude(),
                        currentLocation.getLongitude());

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
            VertexAIResponse aiResponse,
            Double currentLatitude,
            Double currentLongitude) {
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

            // 거리 계산
            Double distance = calculateDistance(
                    currentLatitude,
                    currentLongitude,
                    hospital.getLatitude(),
                    hospital.getLongitude()
            );

            // ETA 계산
            Integer eta = calculateETA(distance);

            // 추천 병원 객체 생성
            PatientRegistrationResponse.RecommendedHospital recommendedHospital =
                    PatientRegistrationResponse.RecommendedHospital.builder()
                            .hospitalId(prediction.getHospitalId())
                            .hospitalDatabaseId(hospital.getId())
                            .hospitalName(hospital.getName())
                            .aiScore(prediction.getScore())
                            .priority(priority++)
                            .aiExplanations(prediction.getExplanations())
                            .distance(distance)
                            .eta(eta)
                            .availableSpecialties(hospital.getSpecialistOncall())
                            .build();

            result.add(recommendedHospital);
        }
        log.info("변환 완료 - 병원 {}개", result.size());
        return result;
    }

    /**
     * Haversine 공식으로 두 지점 사이 거리 계산 (km)
     */
    private Double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        final int R = 6371; // 지구 반지름 (km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return Math.round(R * c * 10.0) / 10.0; // 소수점 1자리
    }

    /**
     * 도착 예상 시간 계산 (분)
     */
    private Integer calculateETA(Double distanceKm) {
        if (distanceKm == null) return null;
        return (int) Math.ceil(distanceKm / 40.0 * 60); // 40km/h 평균 속도
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