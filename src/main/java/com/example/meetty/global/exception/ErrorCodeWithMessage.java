package com.example.meetty.global.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorCodeWithMessage {
    private final String name;
    private final String httpStatus;
    private final String message;

    public ErrorCodeWithMessage(ErrorCode errorCode, String customMessage) {
        this.name = errorCode.name();
        this.httpStatus = errorCode.getHttpStatus().name();
        this.message = customMessage != null ? customMessage : errorCode.getMessage();
    }
}
