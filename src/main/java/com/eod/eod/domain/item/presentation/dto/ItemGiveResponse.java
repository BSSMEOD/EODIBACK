package com.eod.eod.domain.item.presentation.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ItemGiveResponse {

    private final String message;

    public static ItemGiveResponse success() {
        return new ItemGiveResponse("물품 지급이 완료되었습니다.");
    }
}