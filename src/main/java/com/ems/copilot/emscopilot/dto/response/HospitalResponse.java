package com.ems.copilot.emscopilot.dto.response;

import com.ems.copilot.emscopilot.domain.Hospital;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class HospitalResponse {
    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String phone;
    private Integer icuBeds;
    private Integer erBeds;
    private Integer availableBeds;
    private Boolean specialistOncall;
    private Integer hospitalCapacity;
    private Boolean status;

    public static HospitalResponse from(Hospital hospital) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .latitude(hospital.getLatitude())
                .longitude(hospital.getLongitude())
                .phone(hospital.getPhone())
                .icuBeds(hospital.getIcuBeds())
                .erBeds(hospital.getErBeds())
                .availableBeds(hospital.getAvailableBeds())
                .specialistOncall(hospital.getSpecialistOncall())
                .hospitalCapacity(hospital.getHospitalCapacity())
                .status(hospital.getStatus())
                .build();
    }
}
