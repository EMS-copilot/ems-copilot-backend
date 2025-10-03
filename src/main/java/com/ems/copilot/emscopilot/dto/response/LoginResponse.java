package com.ems.copilot.emscopilot.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String employeeNumber;
    private String name;
    private String role;
    private String department;
}
