package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.HospitalResponse;
import com.ems.copilot.emscopilot.service.HospitalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
@Slf4j
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ApiResponse<List<HospitalResponse>> getAllHospitals() {
        List<HospitalResponse> hospitals = hospitalService.getAllHospitals();
        return new ApiResponse<>("SUCCESS", "병원 목록 조회가 성공적으로 처리되었습니다.", hospitals);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ApiResponse<HospitalResponse> getHospitalById(@PathVariable Long id) {
        HospitalResponse hospital = hospitalService.getHospitalById(id);
        return new ApiResponse<>("SUCCESS", "병원 조회가 성공적으로 처리되었습니다.", hospital);
    }

    @GetMapping("/nearby")
    @PreAuthorize("hasAnyRole('PARAMEDIC', 'PARAMEDIC_ADMIN')")
    public ApiResponse<List<HospitalResponse>> getNearbyHospitals(@RequestParam(defaultValue = "10.0") Double distance) {
        List<HospitalResponse> hospitals = hospitalService.getHospitalByDistance(distance);
        return new ApiResponse<>("SUCCESS",
                "거리" + distance + "km 이내 병원 조회가 성공적으로 처리되었습니다.", hospitals);
    }

    /**
     * 전체 병원 랭킹 재계산 (관리자용)
     *
     * POST /api/hospitals/recalculate-ranks
     */
    @PostMapping("/recalculate-ranks")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> recalculateAllRanks() {
        log.info("==== 전체 병원 랭킹 재계산 요청 ====");

        hospitalService.recalculateAllRanks();

        log.info("전체 병원 랭킹 재계산 완료");

        ApiResponse<String> response = new ApiResponse<>(
                "SUCCESS",
                "전체 병원 랭킹이 성공적으로 재계산되었습니다.",
                "지난 7일간의 수용/거절 데이터를 기준으로 랭킹이 업데이트되었습니다."
        );

        return ResponseEntity.ok(response);
    }
}
