package com.eod.eod.domain.item.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClaimItemDto {

    private Long itemId;
    private String itemName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime foundAt;

    private String place;
    private String thumbnailUrl;
    private Long requestCount;
}
