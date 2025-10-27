package com.eod.eod.domain.introduce.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "소개 페이지 수정 요청")
public class IntroduceUpdateRequest {

    @NotBlank(message = "소개 내용은 필수입니다.")
    @JsonProperty("content")
    @Schema(description = "소개 페이지 내용 (마크다운 형식)", example = "분실물 관리 서비스 어디입니다...")
    private String content;
}