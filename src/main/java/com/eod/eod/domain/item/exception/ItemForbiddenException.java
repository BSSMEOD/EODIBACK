package com.eod.eod.domain.item.exception;

import org.springframework.http.HttpStatus;

public class ItemForbiddenException extends ItemException {

    public ItemForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
