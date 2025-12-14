package com.eod.eod.domain.item.presentation.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.eod.eod.common.validation.EnumValue;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.List;

@Data
public class ItemSearchRequest {

    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
    private int page = 1;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
    private int size = 10;

    @JsonProperty("place_ids")
    @Schema(description = "장소 ID 리스트 (선택 사항)", example = "[2,3]")
    private List<Long> placeIds;

    @EnumValue(enumClass = com.eod.eod.domain.item.model.Item.ItemStatus.class,
            message = "유효하지 않은 상태 값입니다.", allowBlank = true)
    @Schema(description = "물품 상태 (LOST, TO_BE_DISCARDED, DISCARDED, GIVEN) - 선택 사항",
            example = "LOST",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String status;
}
