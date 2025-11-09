package com.eod.eod.domain.item.presentation.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ItemDeleteResponse {

    private String message;

    public static ItemDeleteResponse success() {
        return new ItemDeleteResponse("분실물이 성공적으로 삭제되었습니다.");
    }
}
