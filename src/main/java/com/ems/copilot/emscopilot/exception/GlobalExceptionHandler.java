package com.ems.copilot.emscopilot.exception;

import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException e) {
        log.error("CustomException occurred: {} - {}", e.getErrorCode(), e.getMessage());

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus("error");
        response.setMessage(e.getMessage());
        response.setData(null);

        return ResponseEntity.status(e.getErrorCode().getStatus()).body(response);
    }

    /**
     * 권한 없음 (403 Forbidden)
     * @PreAuthorize에서 권한 검사 실패 시 발생
     */
    @ExceptionHandler({AuthorizationDeniedException.class, AccessDeniedException.class})
    public ResponseEntity<ApiResponse<Object>> handleAuthorizationDeniedException(Exception e) {
        log.warn("Authorization denied: {}", e.getMessage());

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus("error");
        response.setMessage("해당 작업을 수행할 권한이 없습니다.");
        response.setData(null);

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    /**
     * 인증 실패 (401 Unauthorized)
     * JWT 토큰이 없거나 유효하지 않을 때 발생
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException e) {
        log.warn("Authentication failed: {}", e.getMessage());

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus("error");
        response.setMessage("인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        response.setData(null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleException(Exception e) {
        log.error("Unexpected error", e);

        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatus("error");
        response.setMessage("서버 에러가 발생했습니다.");
        response.setData(null);

        return ResponseEntity.status(500).body(response);
    }
}