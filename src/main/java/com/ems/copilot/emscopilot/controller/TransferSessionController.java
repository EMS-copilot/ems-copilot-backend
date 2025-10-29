package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.domain.SessionStatus;
import com.ems.copilot.emscopilot.domain.TransferSession;
import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.TransferSessionResponse;
import com.ems.copilot.emscopilot.service.TransferSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * TransferSession 조회 컨트롤러
 */
@RestController
@RequestMapping("/api/transfer-sessions")
@RequiredArgsConstructor
@Slf4j
public class TransferSessionController {

    private final TransferSessionService transferSessionService;

    /**
     * 세션 코드로 세션 정보 조회(단건)
     *
     * GET /api/transfer-sessions/{sessionCode}
     */
    @GetMapping("/{sessionCode}")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN', 'HOSPITAL_STAFF', 'HOSPITAL_ADMIN')")
    public ResponseEntity<ApiResponse<TransferSessionResponse>> getSessionByCode(
            @PathVariable String sessionCode) {

        log.info("==== 세션 정보 조회 ====");
        log.info("세션 코드: {}", sessionCode);

        TransferSession session = transferSessionService.getSessionByCode(sessionCode);

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

    /**
     * 내가 생성한 TransferSession 목록 조회 (전체)
     *
     * GET /api/transfer-sessions/sessions
     */
    @GetMapping("/sessions")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<List<TransferSessionResponse>>> getMyTransferSessions(
            Authentication authentication) {

        log.info("==== 내가 생성한 세션 목록 조회 ====");

        String employeeNumber = authentication.getName();
        List<TransferSession> sessions = transferSessionService.getMyTransferSessions(employeeNumber);

        // Entity -> DTO 변환
        List<TransferSessionResponse> data = sessions.stream()
                .map(TransferSessionResponse::from)
                .toList();

        log.info("세션 목록 조회 완료 - 총 {}개", data.size());

        ApiResponse<List<TransferSessionResponse>> response = new ApiResponse<>(
                "SUCCESS",
                "세션 목록을 성공적으로 조회했습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }

    /**
     * 내가 생성한 TransferSession 목록 조회 (상태별)
     *
     * GET /api/transfer-sessions/sessions/status?status=PENDING
     */
    @GetMapping("/sessions/status")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ResponseEntity<ApiResponse<List<TransferSessionResponse>>> getMyTransferSessionsByStatus(
            @RequestParam SessionStatus status,
            Authentication authentication) {

        log.info("==== 내가 생성한 세션 목록 조회 (상태별) ====");
        log.info("상태: {}", status);

        String employeeNumber = authentication.getName();
        List<TransferSession> sessions = transferSessionService.getMyTransferSessionsByStatus(employeeNumber, status);

        // Entity -> DTO 변환
        List<TransferSessionResponse> data = sessions.stream()
                .map(TransferSessionResponse::from)
                .toList();

        log.info("세션 목록 조회 완료 - 상태: {}, 총 {}개", status, data.size());

        ApiResponse<List<TransferSessionResponse>> response = new ApiResponse<>(
                "SUCCESS",
                "세션 목록을 성공적으로 조회했습니다.",
                data
        );

        return ResponseEntity.ok(response);
    }
}
