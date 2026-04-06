package com.eod.eod.domain.item.exception;

import org.springframework.http.HttpStatus;

public class ItemBadRequestException extends ItemException {

    public ItemBadRequestException(String message) {
        super(HttpStatus.BAD_REQUEST, message);
    }
}
