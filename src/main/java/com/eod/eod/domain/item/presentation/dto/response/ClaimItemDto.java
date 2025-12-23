package com.eod.eod.domain.item.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class ClaimItemDto {

    @Schema(description = "물품 ID", example = "2")
    private Long id;

    @Schema(description = "물품 이름", example = "의자")
    private String name;

    @JsonProperty("foundAt")
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "습득 날짜", example = "2025-12-01")
    private LocalDateTime foundAt;

    @JsonProperty("foundPlace")
    @Schema(description = "습득 장소", example = "강당")
    private String foundPlace;

    @JsonProperty("imageUrl")
    @Schema(description = "이미지 URL", example = "https://example.com/image.jpg")
    private String imageUrl;

    @Schema(description = "회수 신청 건수", example = "1")
    private Long requestCount;
}
