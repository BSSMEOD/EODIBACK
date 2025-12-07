package com.eod.eod.domain.item.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "분실물 검색 요청")
public class ItemSearchRequest {

    @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.")
    @Schema(description = "페이지 번호 (1부터 시작)", example = "1", defaultValue = "1")
    private int page = 1;

    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다.")
    @Schema(description = "페이지 크기", example = "10", defaultValue = "10")
    private int size = 10;

    @JsonProperty("place_id")
    @Schema(description = "장소 ID (선택 사항)", example = "2")
    private Long placeId;

    @NotBlank(message = "물품 상태는 필수입니다.")
    @Schema(description = "물품 상태 (LOST, TO_BE_DISCARDED, DISCARDED, GIVEN)",
            example = "LOST",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String status;
}
