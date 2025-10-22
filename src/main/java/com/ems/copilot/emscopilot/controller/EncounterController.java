package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.domain.Encounter;
import com.ems.copilot.emscopilot.dto.request.ConfirmEncounterRequest;
import com.ems.copilot.emscopilot.dto.response.EncounterResponse;
import com.ems.copilot.emscopilot.service.EncounterService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Encounter 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/encounters")
@RequiredArgsConstructor
@Slf4j
public class EncounterController {

    private final EncounterService encounterService;

    /**
     * 최종 병원 확정 (Encounter 생성)
     *
     * POST /api/encounters/confirm
     */
    @PostMapping("/confirm")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<EncounterResponse> confirmEncounter(
            @Valid @RequestBody ConfirmEncounterRequest request) {

        log.info("==== Encounter 확정 요청 ====");
        log.info("병원 요청 ID: {}", request.getHospitalRequestId());

        Encounter encounter = encounterService.confirmEncounter(request);

        log.info("Encounter 확정 완료 - ID: {}, 환자 코드: {}",
                encounter.getId(), encounter.getPatientCode());

        // Entity -> DTO 변환 (바이탈 정보 제외)
        EncounterResponse response = EncounterResponse.from(encounter);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 병원 완료 처리 (4단계)
     *
     * PUT /api/encounters/{id}/complete
     */
    @PutMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'HOSPITAL_ADMIN')")
    public ResponseEntity<EncounterResponse> completeEncounter(@PathVariable Long id) {

        log.info("==== Encounter 완료 처리 요청 ====");
        log.info("Encounter ID: {}", id);

        Encounter encounter = encounterService.completeEncounter(id);

        log.info("Encounter 완료 처리 성공 - ID: {}, Redis 바이탈 삭제 완료", encounter.getId());

        // Entity -> DTO 변환 (바이탈 정보 제외)
        EncounterResponse response = EncounterResponse.from(encounter);

        return ResponseEntity.ok(response);
    }
}
