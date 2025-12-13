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

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("found_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime foundAt;

    @JsonProperty("found_place")
    private String foundPlace;

    @JsonProperty("found_place_detail")
    private String foundPlaceDetail;
}
