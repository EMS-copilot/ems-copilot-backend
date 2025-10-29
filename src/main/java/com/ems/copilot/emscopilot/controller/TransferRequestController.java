package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.domain.Hospital;
import com.ems.copilot.emscopilot.domain.HospitalRequest;
import com.ems.copilot.emscopilot.domain.TransferSession;
import com.ems.copilot.emscopilot.domain.User;
import com.ems.copilot.emscopilot.dto.request.HospitalResponseRequest;
import com.ems.copilot.emscopilot.dto.request.SendToHospitalsRequest;
import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.HospitalRequestResponse;
import com.ems.copilot.emscopilot.dto.response.SendToHospitalsResponse;
import com.ems.copilot.emscopilot.dto.response.TransferSessionResponse;

import java.util.List;
import com.ems.copilot.emscopilot.exception.CustomException;
import com.ems.copilot.emscopilot.exception.ErrorCode;
import com.ems.copilot.emscopilot.service.TransferRequestService;
import com.ems.copilot.emscopilot.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;



/**
 * 병원 요청 관리 컨트롤러
 */
@RestController
@RequestMapping("/api/hospital-requests")
@RequiredArgsConstructor
@Slf4j
public class TransferRequestController {

    private final TransferRequestService transferRequestService;
    private final UserService userService;

    /**
     * 선택한 병원들에게 환자 정보 전송
     *
     * POST /api/hospital-requests/send
     */
    @PostMapping("/send")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<SendToHospitalsResponse>> sendToHospitals(
            @Valid @RequestBody SendToHospitalsRequest request) {

        log.info("==== 병원 전송 요청 ====");
        log.info("세션 코드: {}", request.getSessionCode());
        log.info("선택한 병원 수: {}", request.getHospitalIds().size());
        log.info("병원 ID 목록: {}", request.getHospitalIds());

        SendToHospitalsResponse data = transferRequestService.sendToHospitals(request);

        log.info("병원 전송 완료 - 총 {}개 병원에 전송됨", data.getTotalSent());

        ApiResponse<SendToHospitalsResponse> response = new ApiResponse<>(
                "SUCCESS",
                data.getTotalSent() + "개 병원에 환자 정보가 전송되었습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 병원 응답 처리 (수용/거절) - 세션 기반
     *
     * PUT /api/hospital-requests/sessions/{sessionCode}/respond
     */
    @PutMapping("/sessions/{sessionCode}/respond")
    @PreAuthorize("hasAnyRole('HOSPITAL_STAFF', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<HospitalRequestResponse>> respondToRequest(
            @PathVariable String sessionCode,
            @Valid @RequestBody HospitalResponseRequest request,
            Authentication authentication) {

        log.info("==== 병원 응답 처리 ====");
        log.info("세션 코드: {}", sessionCode);
        log.info("응답: {}", request.getResponse());
        log.info("메시지: {}", request.getMessage());

        // 1. 현재 로그인한 사용자의 병원 ID 조회
        String employeeNumber = authentication.getName();
        User currentUser = userService.getCurrentUser(employeeNumber);
        Hospital hospital = currentUser.getHospital();

        if (hospital == null) {
            log.error("병원 정보가 없는 사용자 - 사번: {}", employeeNumber);
            throw new CustomException(ErrorCode.USER_HAS_NO_HOSPITAL);
        }

        log.info("병원 ID: {}, 병원명: {}", hospital.getId(), hospital.getName());

        // 2. 병원 응답 처리
        HospitalRequest updatedRequest = transferRequestService.respondToRequest(
                sessionCode,
                hospital.getId(),
                request
        );

        log.info("병원 응답 처리 완료 - 상태: {}", updatedRequest.getStatus());

        // Entity -> DTO 변환
        HospitalRequestResponse data = HospitalRequestResponse.from(updatedRequest);

        ApiResponse<HospitalRequestResponse> response = new ApiResponse<>(
                "SUCCESS",
                "병원 응답이 성공적으로 처리되었습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 세션 코드로 병원 요청 목록 조회
     *
     * GET /api/hospital-requests/sessions/{sessionCode}
     */
    @GetMapping("/sessions/{sessionCode}")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<List<HospitalRequestResponse>>> getRequestsBySessionCode(
            @PathVariable String sessionCode) {

        log.info("==== 병원 요청 목록 조회 ====");
        log.info("세션 코드: {}", sessionCode);

        // 병원 요청 목록 조회
        List<HospitalRequest> requests = transferRequestService.getRequestsBySessionCode(sessionCode);

        // Entity -> DTO 변환
        List<HospitalRequestResponse> data = requests.stream()
                .map(HospitalRequestResponse::from)
                .toList();

        log.info("병원 요청 목록 조회 완료 - 총 {}개", data.size());

        ApiResponse<List<HospitalRequestResponse>> response = new ApiResponse<>(
                "SUCCESS",
                "병원 요청 목록을 성공적으로 조회했습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 세션 코드로 세션 정보 조회
     *
     * GET /api/hospital-requests/sessions/{sessionCode}/info
     */
    @GetMapping("/sessions/{sessionCode}/info")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN', 'HOSPITAL_STAFF', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<TransferSessionResponse>> getSessionInfo(
            @PathVariable String sessionCode) {

        log.info("==== 세션 정보 조회 ====");
        log.info("세션 코드: {}", sessionCode);

        // 세션 조회
        TransferSession session = transferRequestService.getSessionByCode(sessionCode);

        // Entity -> DTO 변환
        TransferSessionResponse data = TransferSessionResponse.from(session);

        log.info("세션 조회 완료 - 환자 코드: {}, 상태: {}", data.getPatientCode(), data.getStatus());

        ApiResponse<TransferSessionResponse> response = new ApiResponse<>(
                "SUCCESS",
                "세션 정보를 성공적으로 조회했습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }
}
