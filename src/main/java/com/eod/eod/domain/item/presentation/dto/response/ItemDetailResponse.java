package com.eod.eod.domain.item.presentation.dto.response;

import com.eod.eod.domain.item.model.Item;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemDetailResponse {

    @Schema(description = "물품 ID", example = "1")
    private Long id;

    @Schema(description = "물품 이름", example = "무테 긱시크 안경")
    private String name;

    @Schema(description = "신고자 학번", example = "1101")
    private Integer reportStudentCode;

    @Schema(description = "신고자 이름", example = "홍길동")
    private String reporterName;

    @JsonProperty("imageUrl")
    @Schema(description = "물품 이미지 URL", example = "")
    private String imageUrl;

    @JsonProperty("foundAt")
    @Schema(description = "습득 일자", example = "2025-06-19")
    private String foundAt;

    @JsonProperty("foundPlace")
    @Schema(description = "습득 장소", example = "기타")
    private String foundPlace;

    @JsonProperty("foundPlaceDetail")
    @Schema(description = "습득 장소 상세", example = "운동장")
    private String foundPlaceDetail;

    @Schema(description = "물품 카테고리", example = "전자기기")
    private Item.ItemCategory category;
}
