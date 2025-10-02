package com.ems.copilot.emscopilot.exception;

import com.ems.copilot.emscopilot.dto.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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