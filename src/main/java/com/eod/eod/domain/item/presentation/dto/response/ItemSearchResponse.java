package com.eod.eod.domain.item.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class ItemSearchResponse {

    @Schema(description = "분실물 목록")
    private List<ItemSummaryResponse> content;

    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @JsonProperty("totalElements")
    @Schema(description = "전체 요소 개수", example = "132")
    private long totalElements;

    @JsonProperty("totalPages")
    @Schema(description = "전체 페이지 수", example = "14")
    private int totalPages;

    @JsonProperty("isLast")
    @Schema(description = "마지막 페이지 여부", example = "false")
    private boolean isLast;

    public static ItemSearchResponse from(Page<ItemSummaryResponse> page) {
        return ItemSearchResponse.builder()
                .content(page.getContent())
                .page(page.getNumber() + 1) // 0-based to 1-based
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isLast(page.isLast())
                .build();
    }
}
