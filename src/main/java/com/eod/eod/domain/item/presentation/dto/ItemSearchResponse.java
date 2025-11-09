package com.eod.eod.domain.item.presentation.dto;

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
@Schema(description = "분실물 검색 응답")
public class ItemSearchResponse {

    @Schema(description = "분실물 목록")
    private List<ItemSummaryResponse> content;

    @Schema(description = "현재 페이지 번호", example = "1")
    private int page;

    @Schema(description = "페이지 크기", example = "10")
    private int size;

    @JsonProperty("total_elements")
    @Schema(description = "전체 요소 개수", example = "132")
    private long totalElements;

    @JsonProperty("total_pages")
    @Schema(description = "전체 페이지 수", example = "14")
    private int totalPages;

    @JsonProperty("is_last")
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
