package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.dto.request.LoginRequest;
import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.LoginResponse;
import com.ems.copilot.emscopilot.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return new ApiResponse<>("SUCCESS", "로그인이 성공적으로 처리되었습니다.", response);
    }
}