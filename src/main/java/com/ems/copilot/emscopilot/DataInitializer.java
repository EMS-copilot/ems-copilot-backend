package com.ems.copilot.emscopilot;

import com.ems.copilot.emscopilot.domain.User;
import com.ems.copilot.emscopilot.domain.UserRole;
import com.ems.copilot.emscopilot.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;

/**
 * 임시 더미데이터
 */
@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // DB에 사용자가 없을 때만 실행
            if (userRepository.count() == 0) {

                List<User> dummyUsers = List.of(
                        // 1. 구급대원(PARAMEDIC) 계정
                        User.builder()
                                .employeeNumber("00010001")
                                .password(passwordEncoder.encode("password"))
                                .name("김구급")
                                .role(UserRole.PARAMEDIC)
                                .department("서울 구급본부")
                                .build(),

                        // 2. 시스템 관리자(SYSTEM_ADMIN) 계정
                        User.builder()
                                .employeeNumber("00020001")
                                .password(passwordEncoder.encode("password"))
                                .name("관리자")
                                .role(UserRole.SYSTEM_ADMIN)
                                .department("시스템 관리팀")
                                .build(),

                        // 3. 병원 직원(HOSPITAL_STAFF) 계정
                        User.builder()
                                .employeeNumber("00030001")
                                .password(passwordEncoder.encode("password"))
                                .name("김간호사")
                                .role(UserRole.HOSPITAL_STAFF)
                                .department("한양병원")
                                .build()
                );

                // 4. 모든 더미 계정을 DB에 저장
                userRepository.saveAll(dummyUsers);
            }
        };
    }
}