package com.ems.copilot.emscopilot.service;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    /**
     * 현재 위치 - 임시 더미데이터
     */
    public CurrentLocation getCurrentLocation(){
        return CurrentLocation.builder()
                .address("충북 음성군 금왕읍 음성로1340번길 31-1 한울요양원")
                .latitude(37.039750)
                .longitude(127.562095)
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentLocation {
        private String address;
        private Double latitude;
        private Double longitude;
    }
}
