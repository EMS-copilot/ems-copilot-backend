package com.ems.copilot.emscopilot.controller;

import com.ems.copilot.emscopilot.domain.User;
import com.ems.copilot.emscopilot.dto.request.LoginRequest;
import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import com.ems.copilot.emscopilot.dto.response.LoginResponse;
import com.ems.copilot.emscopilot.service.AuthService;
import com.ems.copilot.emscopilot.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;
    private final UserService userService;

    // 로그인
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return new ApiResponse<>("SUCCESS", "로그인이 성공적으로 처리되었습니다.", response);
    }

    // 내 프로필 조회
    @GetMapping("/me")
    public ApiResponse<User> getCurrentUser(Authentication authentication) {
        String employeeNumber = authentication.getName();
        User user = userService.getCurrentUser(employeeNumber);
        return new ApiResponse<>("SUCCESS", "프로필 조회가 성공적으로 처리되었습니다.", user);
    }

    // 내 역할 조회
    @GetMapping("/me/role")
    public ApiResponse<String> getMyRole(Authentication authentication) {
        String employeeNumber = authentication.getName();
        User user = userService.getCurrentUser(employeeNumber);
        return new ApiResponse<>("SUCCESS", "역할 조회가 성공적으로 처리되었습니다.", user.getRole().name());
    }

}