package com.ems.copilot.emscopilot.dto.response;

import com.ems.copilot.emscopilot.domain.TransferSession;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 세션 조회 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferSessionResponse {

    private String sessionCode;
    private String patientCode;
    private String patientTempId;
    private String status;
    private List<String> chiefComplaints;
    private String currentAddress;
    private Double currentLatitude;
    private Double currentLongitude;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;

    /**
     * Entity -> DTO 변환
     */
    public static TransferSessionResponse from(TransferSession session) {
        return TransferSessionResponse.builder()
                .sessionCode(session.getSessionCode())
                .patientCode(session.getPatientCode())
                .patientTempId(session.getPatientTempId())
                .status(session.getStatus().name())
                .chiefComplaints(session.getChiefComplaintList())
                .currentAddress(session.getCurrentAddress())
                .currentLatitude(session.getCurrentLatitude())
                .currentLongitude(session.getCurrentLongitude())
                .createdAt(session.getCreatedAt())
                .expiresAt(session.getExpiresAt())
                .build();
    }
}
