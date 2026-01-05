package com.eod.eod.domain.item.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.eod.eod.common.validation.EnumValue;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

@Data
public class ItemSearchRequest {

    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
    private int page = 1;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
    private int size = 10;

    @Schema(description = "검색어 (선택 사항)",
            example = "아이폰",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String query;

    @JsonProperty("place_ids")
    @Schema(description = "장소 ID 리스트 (선택 사항)", example = "[2,3]")
    private List<Long> placeIds;

    @EnumValue(enumClass = com.eod.eod.domain.item.model.Item.ItemStatus.class,
            message = "유효하지 않은 상태 값입니다.", allowBlank = true)
    @Schema(description = "물품 상태 (LOST, TO_BE_DISCARDED, DISCARDED, GIVEN) - 선택 사항",
            example = "LOST",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "습득일 시작 날짜 (선택 사항)", example = "2024-01-01",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate foundAtFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "습득일 종료 날짜 (선택 사항)", example = "2024-12-31",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate foundAtTo;

    @Schema(hidden = true)
    public void setFound_at_from(LocalDate foundAtFrom) {
        this.foundAtFrom = foundAtFrom;
    }

    @Schema(hidden = true)
    public void setFound_at_to(LocalDate foundAtTo) {
        this.foundAtTo = foundAtTo;
    }

    @com.eod.eod.common.validation.ItemCategoriesValue(
            message = "유효하지 않은 카테고리 값입니다.")
    @Schema(description = "물품 카테고리 리스트 (교복, 체육복, 단체복, 사복, 무선 이어폰, 전자기기, 안경, 기타) - 선택 사항, 다중 선택 가능",
            example = "[\"교복\", \"체육복\"]",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<String> categories;

    @EnumValue(enumClass = ItemSearchSort.class, message = "유효하지 않은 정렬 값입니다.")
    @Schema(description = "정렬 순서 (LATEST: 최신순, OLDEST: 과거순) - 선택 사항, 기본 LATEST(최신순)",
            example = "LATEST",
            allowableValues = {"LATEST", "OLDEST"},
            defaultValue = "LATEST",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String sort = ItemSearchSort.LATEST.name();
}
