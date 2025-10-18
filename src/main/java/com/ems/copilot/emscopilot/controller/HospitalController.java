package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.HospitalResponse;
import com.ems.copilot.emscopilot.service.HospitalService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hospitals")
@RequiredArgsConstructor
public class HospitalController {

    private final HospitalService hospitalService;

    @GetMapping
    public ApiResponse<List<HospitalResponse>> getAllHospitals() {
        List<HospitalResponse> hospitals = hospitalService.getAllHospitals();
        return new ApiResponse<>("SUCCESS", "병원 목록 조회가 성공적으로 처리되었습니다.", hospitals);
    }

    @GetMapping("/{id}")
    public ApiResponse<HospitalResponse> getHospitalById(@PathVariable Long id) {
        HospitalResponse hospital = hospitalService.getHospitalById(id);
        return new ApiResponse<>("SUCCESS", "병원 조회가 성공적으로 처리되었습니다.", hospital);
    }
}
