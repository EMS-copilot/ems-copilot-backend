package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.HospitalResponse;
import com.ems.copilot.emscopilot.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
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
}
