package com.example.meetty.global.dto;

import com.example.meetty.global.exception.AppException;
import com.example.meetty.global.exception.ErrorCode;
import com.example.meetty.global.exception.ErrorCodeWithMessage;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    Boolean isSuccess;
    T data;
    Object errorCode;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, data, null);
    }

    public static <T> ApiResponse<T> fail(ErrorCode errorCode) {
        return new ApiResponse<>(false, null, errorCode);
    }

    public static <T> ApiResponse<T> fail(AppException e) {
        return new ApiResponse<>(false, null, new ErrorCodeWithMessage(e.getErrorCode(), e.getCustomMessage()));
    }
}
