package com.ems.copilot.emscopilot.util;

import com.ems.copilot.emscopilot.dto.response.ApiResponse;

public class ResponseUtil {
    public static <T>ApiResponse<T> success(String message, T data){
        return new ApiResponse<>("success", message, data);
    }

    public static <T>ApiResponse<T> error(String message, T data){
        return new ApiResponse<>("error", message, data);
    }
}
