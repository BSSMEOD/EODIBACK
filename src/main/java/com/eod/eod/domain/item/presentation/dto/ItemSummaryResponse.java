package com.eod.eod.domain.item.presentation.dto;

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
@Schema(description = "분실물 요약 정보")
public class ItemSummaryResponse {

    @Schema(description = "물품 ID", example = "101")
    private Long id;

    @Schema(description = "물품 이름", example = "무선 이어폰")
    private String name;

    @JsonProperty("found_date")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Schema(description = "습득 날짜", example = "2025-07-01")
    private LocalDateTime foundDate;

    @JsonProperty("found_place")
    @Schema(description = "습득 장소", example = "SRC")
    private String foundPlace;

    @JsonProperty("place_detail")
    @Schema(description = "장소 상세 정보", example = "3층 남자기숙사 중앙홀")
    private String placeDetail;

    @JsonProperty("thumbnail_url")
    @Schema(description = "썸네일 이미지 URL", example = "")
    private String thumbnailUrl;

    @Schema(description = "물품 상태", example = "LOST", allowableValues = {"LOST", "TO_BE_DISCARDED", "DISCARDED", "GIVEN"})
    private String status;
}
