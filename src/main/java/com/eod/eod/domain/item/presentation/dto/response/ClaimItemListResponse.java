package com.eod.eod.domain.item.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ClaimItemListResponse {

    private List<ClaimItemDto> items;

    public static ClaimItemListResponse of(List<ClaimItemDto> items) {
        return new ClaimItemListResponse(items);
    }
}
