package com.eod.eod.domain.item.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ItemDetailResponse {

    private Long id;

    private String name;

    @JsonProperty("imageUrl")
    private String imageUrl;

    @JsonProperty("foundAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime foundAt;

    @JsonProperty("foundPlace")
    private String foundPlace;

    @JsonProperty("foundPlaceDetail")
    private String foundPlaceDetail;
}
