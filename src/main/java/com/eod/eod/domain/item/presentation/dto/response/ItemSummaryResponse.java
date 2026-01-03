package com.eod.eod.domain.item.presentation.dto.response;

import com.eod.eod.domain.item.model.Item;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ItemSummaryResponse {

    @Schema(description = "물품 ID", example = "101")
    private Long id;

    @Schema(description = "물품 이름", example = "무선 이어폰")
    private String name;

    @Schema(description = "신고자 이름", example = "홍길동")
    private String reporterName;

    @JsonProperty("foundAt")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "습득 날짜", example = "2025-07-01")
    private LocalDateTime foundAt;

    @JsonProperty("foundPlace")
    @Schema(description = "습득 장소", example = "SRC")
    private String foundPlace;

    @JsonProperty("placeDetail")
    @Schema(description = "장소 상세 정보", example = "3층 남자기숙사 중앙홀")
    private String placeDetail;

    @JsonProperty("imageUrl")
    @Schema(description = "이미지 URL", example = "")
    private String imageUrl;

    @Schema(description = "물품 상태", example = "LOST", allowableValues = {"LOST", "TO_BE_DISCARDED", "DISCARDED", "GIVEN"})
    private String status;

    @Schema(description = "물품 카테고리", example = "무선 이어폰")
    private Item.ItemCategory category;

    @Schema(description = "폐기 예정일", example = "2024-12-31")
    String disposalDate;
}
