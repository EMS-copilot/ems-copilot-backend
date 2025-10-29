package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.domain.Encounter;
import com.ems.copilot.emscopilot.domain.PatientVitalData;
import com.ems.copilot.emscopilot.domain.SessionStatus;
import com.ems.copilot.emscopilot.dto.request.ConfirmEncounterRequest;
import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.EncounterResponse;
import com.ems.copilot.emscopilot.service.EncounterService;
import com.ems.copilot.emscopilot.service.SessionStorageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Encounter 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/encounters")
@RequiredArgsConstructor
@Slf4j
public class EncounterController {

    private final EncounterService encounterService;
    private final SessionStorageService sessionStorageService;

    /**
     * 최종 병원 확정 (Encounter 생성)
     *
     * POST /api/encounters/confirm
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<EncounterResponse>> confirmEncounter(
            @Valid @RequestBody ConfirmEncounterRequest request) {

        log.info("==== Encounter 확정 요청 ====");
        log.info("병원 요청 ID: {}", request.getHospitalRequestId());

        Encounter encounter = encounterService.confirmEncounter(request);

        log.info("Encounter 확정 완료 - ID: {}, 환자 코드: {}",
                encounter.getId(), encounter.getPatientCode());

        // Redis에서 바이탈 정보 조회
        String sessionCode = encounter.getSessionCode();
        PatientVitalData vitalData = sessionStorageService.getVitalData(sessionCode)
                .orElse(null);

        EncounterResponse data;
        if (vitalData != null) {
            // 바이탈 정보를 포함하여 응답 생성
            EncounterResponse.VitalInfo vitalInfo = EncounterResponse.VitalInfo.builder()
                    .sbp(vitalData.getSbp())
                    .dbp(vitalData.getDbp())
                    .hr(vitalData.getHr())
                    .rr(vitalData.getRr())
                    .spo2(vitalData.getSpo2())
                    .temp(vitalData.getTemp())
                    .symptoms(vitalData.getSymptoms())
                    .build();

            data = EncounterResponse.fromWithVital(encounter, vitalInfo);
            log.info("바이탈 정보 포함하여 응답 생성 완료");
        } else {
            // 바이탈 정보 없이 응답 (만료된 경우)
            data = EncounterResponse.from(encounter);
            log.warn("바이탈 정보 없음 (만료 또는 삭제됨) - 세션 코드: {}", sessionCode);
        }

        ApiResponse<EncounterResponse> response = new ApiResponse<>(
                "SUCCESS",
                "병원이 성공적으로 확정되었습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 병원 도착 처리 (이송 완료)
     *
     * PUT /api/encounters/{id}/arrive
     */
    @PutMapping("/{id}/arrive")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<EncounterResponse>> arriveAtHospital(@PathVariable Long id) {

        log.info("==== 병원 도착 처리 요청 ====");
        log.info("Encounter ID: {}", id);

        Encounter encounter = encounterService.arriveAtHospital(id);

        log.info("병원 도착 처리 성공 - ID: {}, 상태: ARRIVED", encounter.getId());

        // Entity -> DTO 변환 (바이탈 정보 제외)
        EncounterResponse data = EncounterResponse.from(encounter);

        ApiResponse<EncounterResponse> response = new ApiResponse<>(
                "SUCCESS",
                "병원 도착이 성공적으로 처리되었습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 병원 처치 완료 처리
     *
     * PUT /api/encounters/{id}/complete
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<EncounterResponse>> completeEncounter(@PathVariable Long id) {

        log.info("==== Encounter 완료 처리 요청 ====");
        log.info("Encounter ID: {}", id);

        Encounter encounter = encounterService.completeEncounter(id);

        log.info("Encounter 완료 처리 성공 - ID: {}, 상태: COMPLETED, Redis 바이탈 삭제 완료", encounter.getId());

        // Entity -> DTO 변환 (바이탈 정보 제외)
        EncounterResponse data = EncounterResponse.from(encounter);

        ApiResponse<EncounterResponse> response = new ApiResponse<>(
                "SUCCESS",
                "처치가 성공적으로 완료되었습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 내가 담당한 이송 내역 조회 (전체)
     *
     * GET /api/encounters/my-encounters
     */
    @GetMapping("/my-encounters")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<List<EncounterResponse>>> getMyEncounters(
            Authentication authentication) {

        log.info("==== 내가 담당한 이송 내역 조회 ====");

        String employeeNumber = authentication.getName();
        List<EncounterResponse> data = encounterService.getMyEncounters(employeeNumber);

        log.info("이송 내역 조회 완료 - 총 {}개", data.size());

        ApiResponse<List<EncounterResponse>> response = new ApiResponse<>(
                "SUCCESS",
                "이송 내역을 성공적으로 조회했습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 내가 담당한 이송 내역 조회 (상태별)
     *
     * GET /api/encounters/my-encounters/status?status=IN_TRANSIT
     */
    @GetMapping("/my-encounters/status")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<List<EncounterResponse>>> getMyEncountersByStatus(
            @RequestParam SessionStatus status,
            Authentication authentication) {

        log.info("==== 내가 담당한 이송 내역 조회 (상태별) ====");
        log.info("상태: {}", status);

        String employeeNumber = authentication.getName();
        List<EncounterResponse> data = encounterService.getMyEncountersByStatus(employeeNumber, status);

        log.info("이송 내역 조회 완료 - 상태: {}, 총 {}개", status, data.size());

        ApiResponse<List<EncounterResponse>> response = new ApiResponse<>(
                "SUCCESS",
                "이송 내역을 성공적으로 조회했습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }
}
