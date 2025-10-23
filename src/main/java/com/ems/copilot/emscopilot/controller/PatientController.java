package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.dto.request.PatientDataRequest;
import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.PatientRegistrationResponse;
import com.ems.copilot.emscopilot.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/patients")
@RequiredArgsConstructor
@Slf4j
public class PatientController {

    private final PatientService patientService;

    /**
     * 환자 정보 등록
     */
    @PostMapping("/register")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<PatientRegistrationResponse>> registerPatient(
            @Valid @RequestBody PatientDataRequest request){
        log.info("==== 환자 정보 등록 요청 ====");
        log.info("나이: {}, 성별; {}, KTAS: {}", request.getAge(), request.getSex(), request.getTriageLevel());
        log.info("증상: {}", request.getSymptoms());
        log.info("바이탈 - SBP: {}, DBP: {}, HR: {},RR: {},Sp02: {},Temp: {}",
                request.getSbp(), request.getDbp(), request.getHr(),
                request.getRr(), request.getSpo2(), request.getTemp());

        // 서비스 호출
        PatientRegistrationResponse data = patientService.registerPatient(request);

        log.info("환자 등록 완료 - 세션 코드: {}", data.getSessionCode());

        ApiResponse<PatientRegistrationResponse> response = new ApiResponse<>(
                "SUCCESS",
                "환자 정보가 성공적으로 등록되었습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }
}
