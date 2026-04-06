package com.eod.eod.domain.item.exception;

import org.springframework.http.HttpStatus;

public abstract class ItemException extends RuntimeException {

    private final HttpStatus status;

    protected ItemException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
