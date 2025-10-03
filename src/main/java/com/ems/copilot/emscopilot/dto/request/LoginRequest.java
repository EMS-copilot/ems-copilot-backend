package com.ems.copilot.emscopilot.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String employeeNumber;
    private String password;
}
