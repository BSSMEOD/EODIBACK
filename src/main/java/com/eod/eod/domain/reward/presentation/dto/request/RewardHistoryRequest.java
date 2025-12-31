package com.eod.eod.domain.reward.presentation.dto.request;

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
public class RewardHistoryRequest {

    // 기존 파라미터 (하위 호환성 유지)
    @Schema(description = "조회할 사용자 ID (레거시)", example = "1", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long userId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "조회 날짜 (레거시)", example = "2025-08-05", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate date;

    @Schema(description = "학년", example = "3", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer grade;

    @JsonProperty("class")
    @Schema(name = "class", description = "반", example = "2", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Integer classNumber;

    // 새로운 검색 파라미터
    @Schema(description = "아이템 ID로 필터링", example = "10", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long itemId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "시작 날짜 (inclusive)", example = "2025-12-01", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Schema(description = "종료 날짜 (exclusive)", example = "2025-12-31", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private LocalDate to;

    // Setters for Spring to bind request parameters
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    // URL 파라미터 'class'를 classNumber 필드에 매핑하기 위한 Setter
    public void setClass(Integer classNumber) {
        this.classNumber = classNumber;
    }

    public void setItemId(Long itemId) {
        this.itemId = itemId;
    }

    public void setFrom(LocalDate from) {
        this.from = from;
    }

    public void setTo(LocalDate to) {
        this.to = to;
    }

    /**
     * 레거시 date 파라미터가 있으면 from 파라미터로 매핑
     */
    public LocalDate getEffectiveFrom() {
        return date != null ? date : from;
    }

    /**
     * 레거시 userId와 새로운 검색의 userId 통합
     */
    public Long getEffectiveUserId() {
        return userId;
    }
}
