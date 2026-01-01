package com.eod.eod.common.exception;

import com.eod.eod.domain.item.exception.InvalidParameterException;
import com.eod.eod.domain.image.exception.ImageException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // 잘못된 파라미터 요청 (400)
    @ExceptionHandler(InvalidParameterException.class)
    public ResponseEntity<ErrorResponse> handleInvalidParameterException(InvalidParameterException invalidParameterException) {
        return buildResponse(HttpStatus.BAD_REQUEST, invalidParameterException.getMessage());
    }

    @ExceptionHandler(ImageException.class)
    public ResponseEntity<ErrorResponse> handleImageException(ImageException imageException) {
        return buildResponse(imageException.getErrorCode().getStatus(), imageException.getErrorCode().getMessage());
    }

    // 물품을 찾을 수 없는 경우 (404)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException illegalArgumentException) {
        return buildResponse(HttpStatus.NOT_FOUND, illegalArgumentException.getMessage());
    }

    // 이미 지급된 물품인 경우 (400)
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(IllegalStateException illegalStateException) {
        return buildResponse(HttpStatus.BAD_REQUEST, illegalStateException.getMessage());
    }

    // 인증 실패 (401)
    @ExceptionHandler({
            AuthenticationException.class,
            InsufficientAuthenticationException.class,
            BadCredentialsException.class
    })
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException authenticationException) {
        log.warn("인증 실패: {}", authenticationException.getMessage());
        return buildResponse(HttpStatus.UNAUTHORIZED, "인증이 필요합니다. 로그인 후 다시 시도해주세요.");
    }

    // ADMIN 권한이 없는 경우 (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException accessDeniedException) {
        return buildResponse(HttpStatus.FORBIDDEN, accessDeniedException.getMessage());
    }

    // @RequestParam, @PathVariable 검증 실패 (400)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(ConstraintViolationException constraintViolationException) {
        String message = constraintViolationException.getConstraintViolations().stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.joining(", "));
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 요청 데이터 검증 실패 (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException methodArgumentNotValidException) {
        Map<String, String> errors = new HashMap<>();
        methodArgumentNotValidException.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }

    // 지원하지 않는 HTTP Method (405)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException exception) {
        String supported = exception.getSupportedHttpMethods() != null
                ? exception.getSupportedHttpMethods().toString()
                : "없음";
        String message = String.format("지원하지 않는 HTTP 메서드입니다. 지원되는 메서드: %s", supported);
        return buildResponse(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    // 지원하지 않는 Content-Type (415)
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException exception) {
        String supported = exception.getSupportedMediaTypes().isEmpty()
                ? "없음"
                : exception.getSupportedMediaTypes().toString();
        String message = String.format("지원하지 않는 Content-Type 입니다. 지원되는 타입: %s", supported);
        return buildResponse(HttpStatus.UNSUPPORTED_MEDIA_TYPE, message);
    }

    // 허용되지 않는 Accept 헤더 (406)
    @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException exception) {
        String supported = exception.getSupportedMediaTypes().isEmpty()
                ? "없음"
                : exception.getSupportedMediaTypes().toString();
        String message = String.format("허용되지 않는 응답 타입입니다. 요청 가능한 타입: %s", supported);
        return buildResponse(HttpStatus.NOT_ACCEPTABLE, message);
    }

    // 요청 본문 파싱 실패 (400)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadable(HttpMessageNotReadableException exception) {
        String message = "요청 본문을 읽을 수 없습니다. 요청 형식을 확인해주세요.";
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 필수 파라미터 누락 (400)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(MissingServletRequestParameterException exception) {
        String message = String.format("필수 파라미터(%s)가 누락되었습니다.", exception.getParameterName());
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 타입 변환 실패 (400)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException exception) {
        String requiredType = exception.getRequiredType() != null
                ? exception.getRequiredType().getSimpleName()
                : "요구되는 타입";
        String message = String.format("파라미터 %s 의 값 '%s'를 %s 타입으로 변환할 수 없습니다.",
                exception.getName(), exception.getValue(), requiredType);
        return buildResponse(HttpStatus.BAD_REQUEST, message);
    }

    // 업로드 용량 초과 (413)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exception) {
        String message = "업로드 가능한 파일 크기를 초과했습니다.";
        return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, message);
    }

    // 데이터 무결성 위반 (409)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException exception) {
        String message = "데이터 무결성 제약 조건을 위반했습니다.";
        return buildResponse(HttpStatus.CONFLICT, message);
    }

    // 기타 예상치 못한 오류 (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception exception) {
        log.error("Unhandled exception", exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus httpStatus, String message) {
        return ResponseEntity.status(httpStatus)
                .body(ErrorResponse.of(httpStatus, message, getCurrentRequestPath()));
    }

    private String getCurrentRequestPath() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return "";
        }
        return attributes.getRequest().getRequestURI();
    }
}
