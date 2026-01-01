package com.eod.eod.domain.image.exception;

/**
 * 이미지 도메인 예외
 */
public class ImageException extends RuntimeException {

    private final ImageErrorCode errorCode;

    public ImageException(ImageErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public ImageException(ImageErrorCode errorCode, Throwable cause) {
        super(errorCode.getMessage(), cause);
        this.errorCode = errorCode;
    }

    public ImageErrorCode getErrorCode() {
        return errorCode;
    }
}
