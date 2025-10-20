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
    private String externalId;
    private Double distance;
    private Integer eta;
    private Character rank;
    private String name;
    private String address;
    private Integer icuBeds;
    private Integer erBeds;
    private Integer hospitalCapacity;

    public static HospitalResponse response(Hospital hospital) {
        return HospitalResponse.builder()
                .id(hospital.getId())
                .externalId(hospital.getExternalId())
                .distance(hospital.getDistance())
                .eta(hospital.getEta())
                .rank(hospital.getRank())
                .name(hospital.getName())
                .address(hospital.getAddress())
                .icuBeds(hospital.getIcuBeds())
                .erBeds(hospital.getErBeds())
                .hospitalCapacity(hospital.getHospitalCapacity())
                .build();
    }
}
