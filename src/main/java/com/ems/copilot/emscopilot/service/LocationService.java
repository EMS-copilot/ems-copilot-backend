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
                .address("충청북도 음성군 음성읍 중앙로 195")
                .latitude(36.9401)
                .longitude(127.6922)
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
