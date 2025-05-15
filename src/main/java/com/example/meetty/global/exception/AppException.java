package com.example.meetty.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class AppException extends RuntimeException{
    private final ErrorCode errorCode;
    private final String customMessage;

    public AppException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.customMessage = errorCode.getMessage();
    }

    public AppException(ErrorCode errorCode, String customMessage) {
        super(customMessage);
        this.errorCode = errorCode;
        this.customMessage = customMessage;
    }
}
