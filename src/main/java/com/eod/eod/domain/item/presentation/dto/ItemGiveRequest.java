package com.eod.eod.domain.item.presentation.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemGiveRequest {

    @NotNull(message = "학생 ID는 필수입니다.")
    @JsonProperty("student_id")
    private Long studentId;
}