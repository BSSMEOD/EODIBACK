package com.eod.eod.domain.item.exception;

import org.springframework.http.HttpStatus;

public class ItemResourceNotFoundException extends ItemException {

    public ItemResourceNotFoundException(String message) {
        super(HttpStatus.NOT_FOUND, message);
    }
}
