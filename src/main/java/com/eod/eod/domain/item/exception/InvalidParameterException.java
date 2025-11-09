package com.eod.eod.domain.item.exception;

/**
 * 잘못된 파라미터 요청 시 발생하는 예외
 * HTTP 400 Bad Request로 처리됩니다.
 */
public class InvalidParameterException extends RuntimeException {

    public InvalidParameterException(String message) {
        super(message);
    }

    public InvalidParameterException(String message, Throwable cause) {
        super(message, cause);
    }
}
