package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.domain.Hospital;
import com.ems.copilot.emscopilot.dto.response.HospitalResponse;
import com.ems.copilot.emscopilot.exception.CustomException;
import com.ems.copilot.emscopilot.exception.ErrorCode;
import com.ems.copilot.emscopilot.repository.HospitalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HospitalService {

    private final HospitalRepository hospitalRepository;

    public List<HospitalResponse> getAllHospitals() {
        List<Hospital> hospitals = hospitalRepository.findAll();
        return hospitals.stream()
                .map(HospitalResponse::from)
                .collect(Collectors.toList());
    }

    public HospitalResponse getHospitalById(Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
        return HospitalResponse.from(hospital);
    }
}
