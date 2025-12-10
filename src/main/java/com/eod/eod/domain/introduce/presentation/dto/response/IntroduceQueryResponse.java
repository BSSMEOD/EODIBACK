package com.eod.eod.domain.introduce.presentation.dto.response;

import com.eod.eod.domain.introduce.model.Introduce;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@Builder
@Schema(
    description = "소개 페이지 조회 응답",
    example = "{\"content\": \"분실물 관리 서비스 '어디'입니다. 사용 방법은 1) 검색으로 분실물을 찾아보고 2) 없다면 등록을 통해 제보를 남겨주세요.\", \"updated_at\": \"2025-10-24T00:00:00Z\"}"
)
public class IntroduceQueryResponse {

    @JsonProperty("content")
    @Schema(description = "소개 페이지 내용 (마크다운 형식)", example = "분실물 관리 서비스 '어디'입니다. 사용 방법은 1) 검색으로 분실물을 찾아보고 2) 없다면 등록을 통해 제보를 남겨주세요.")
    private String content;

    @JsonProperty("updatedAt")
    @Schema(description = "마지막 수정 시간", example = "2025-10-24T00:00:00Z")
    private String updatedAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    public static IntroduceQueryResponse from(Introduce introduce) {
        return IntroduceQueryResponse.builder()
                .content(introduce.getContent())
                .updatedAt(introduce.getUpdatedAt().format(FORMATTER))
                .build();
    }
}
