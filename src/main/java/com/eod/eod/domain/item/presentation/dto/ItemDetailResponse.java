package com.eod.eod.domain.item.presentation.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(
    description = "물품 상세 조회 응답",
    example = """
        {
            "id": 1,
            "name": "무테 긱시크 안경",
            "image_url": "",
            "found_at": "2025-06-19 12:20",
            "found_place": "기타",
            "found_place_detail": "운동장"
        }
        """
)
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
