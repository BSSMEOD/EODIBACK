package com.eod.eod.domain.introduce.presentation.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class IntroduceUpdateResponse {

    @JsonProperty("message")
    @Schema(description = "응답 메시지", example = "소개 페이지가 성공적으로 수정되었습니다.")
    private String message;

    public static IntroduceUpdateResponse success() {
        return IntroduceUpdateResponse.builder()
                .message("소개 페이지가 성공적으로 수정되었습니다.")
                .build();
    }
}
