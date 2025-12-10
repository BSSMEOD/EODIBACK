package com.eod.eod.domain.reward.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Schema(description = "상점 지급 이력 조회 요청")
public class RewardHistoryRequest {

    @Schema(description = "조회할 사용자 ID (user_id 단독 사용)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "조회 날짜 (date+grade+class 함께 사용)", example = "2025-08-05", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate date;

    @Schema(description = "학년 (date+grade+class 함께 사용)", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer grade;

    @JsonProperty("class")
    @Schema(name = "class", description = "반 (date+grade+class 함께 사용)", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer classNumber;

    public boolean isUserQuery() {
        return userId != null && date == null && grade == null && classNumber == null;
    }

    public boolean isClassQuery() {
        return userId == null && date != null && grade != null && classNumber != null;
    }

    public void validate() {
        if (isUserQuery() || isClassQuery()) {
            return;
        }
        throw new IllegalArgumentException("user_id만 제공하거나 date, grade, class를 모두 제공해야 합니다.");
    }
}
