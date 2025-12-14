package com.eod.eod.domain.item.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ItemDetailResponse {

    @Schema(description = "물품 ID", example = "1")
    private Long id;

    @Schema(description = "물품 이름", example = "무테 긱시크 안경")
    private String name;

    @JsonProperty("imageUrl")
    @Schema(description = "물품 이미지 URL", example = "")
    private String imageUrl;

    @JsonProperty("foundAt")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Schema(description = "습득 일자 및 시간", example = "2025-06-19 12:20")
    private LocalDateTime foundAt;

    @JsonProperty("foundPlace")
    @Schema(description = "습득 장소", example = "기타")
    private String foundPlace;

    @JsonProperty("foundPlaceDetail")
    @Schema(description = "습득 장소 상세", example = "운동장")
    private String foundPlaceDetail;
}
