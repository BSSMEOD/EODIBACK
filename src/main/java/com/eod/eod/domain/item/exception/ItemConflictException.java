package com.eod.eod.domain.item.exception;

import org.springframework.http.HttpStatus;

public class ItemConflictException extends ItemException {

    public ItemConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
