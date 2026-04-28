package com.eod.eod.domain.discord.presentation;

import com.eod.eod.domain.discord.exception.DiscordVerifyException;
import com.eod.eod.domain.discord.presentation.dto.response.DiscordVerifyResponse;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@Order(Ordered.HIGHEST_PRECEDENCE)
@RestControllerAdvice(assignableTypes = DiscordController.class)
public class DiscordExceptionHandler {

    @ExceptionHandler(DiscordVerifyException.class)
    public ResponseEntity<DiscordVerifyResponse> handleDiscordVerifyException(DiscordVerifyException exception) {
        return ResponseEntity.status(exception.getHttpStatus())
                .body(DiscordVerifyResponse.fail(
                        exception.getStatus(),
                        exception.getCode(),
                        exception.isRequiresStudentId(),
                        exception.getMessage()
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<DiscordVerifyResponse> handleIllegalArgumentException(IllegalArgumentException exception) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(DiscordVerifyResponse.fail("invalid_request", "INVALID_REQUEST", false, exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<DiscordVerifyResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String message = exception.getBindingResult().getAllErrors().stream()
                .map(error -> ((FieldError) error).getDefaultMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(DiscordVerifyResponse.fail("invalid_request", "INVALID_REQUEST", false, message));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<DiscordVerifyResponse> handleConstraintViolationException(ConstraintViolationException exception) {
        String message = exception.getConstraintViolations().stream()
                .map(violation -> violation.getMessage())
                .findFirst()
                .orElse("잘못된 요청입니다.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(DiscordVerifyResponse.fail("invalid_request", "INVALID_REQUEST", false, message));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<DiscordVerifyResponse> handleException(Exception exception) {
        log.error("Unhandled discord verify exception", exception);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(DiscordVerifyResponse.internalServerError());
    }
}
