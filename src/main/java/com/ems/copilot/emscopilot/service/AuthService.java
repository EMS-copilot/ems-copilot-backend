package com.ems.copilot.emscopilot.service;

import com.ems.copilot.emscopilot.config.JwtTokenProvider;
import com.ems.copilot.emscopilot.domain.User;
import com.ems.copilot.emscopilot.dto.request.LoginRequest;
import com.ems.copilot.emscopilot.dto.response.LoginResponse;
import com.ems.copilot.emscopilot.exception.CustomException;
import com.ems.copilot.emscopilot.exception.ErrorCode;
import com.ems.copilot.emscopilot.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmployeeNumber(request.getEmployeeNumber())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new CustomException(ErrorCode.INVALID_PASSWORD);
        }

        String token = jwtTokenProvider.generateToken(user.getEmployeeNumber(), user.getRole().name());

        return new LoginResponse(
                token,
                user.getEmployeeNumber(),
                user.getName(),
                user.getRole().name(),
                user.getDepartment());
    }
}
