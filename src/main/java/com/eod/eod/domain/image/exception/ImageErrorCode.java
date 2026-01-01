package com.eod.eod.domain.image.exception;

import org.springframework.http.HttpStatus;

public enum ImageErrorCode {
    EMPTY_FILE(HttpStatus.BAD_REQUEST, "업로드된 파일이 비어있습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "이미지 파일만 업로드할 수 있습니다."),
    FILE_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "업로드 가능한 파일 크기를 초과했습니다."),
    FILE_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "이미지 저장에 실패했습니다.");

    private final HttpStatus status;
    private final String message;

    ImageErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
