package com.eod.eod.common.exception;

import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {
    private final String message;
    private final int status;
    private final String error;
    private final String path;
    private final LocalDateTime timestamp;

    @Builder
    private ErrorResponse(String message, int status, String error, String path, LocalDateTime timestamp) {
        this.message = message;
        this.status = status;
        this.error = error;
        this.path = path;
        this.timestamp = timestamp;
    }

    public static ErrorResponse of(HttpStatus httpStatus, String message, String path) {
        return ErrorResponse.builder()
                .message(message)
                .status(httpStatus.value())
                .error(httpStatus.getReasonPhrase())
                .path(path)
                .timestamp(LocalDateTime.now())
                .build();
    }
}
