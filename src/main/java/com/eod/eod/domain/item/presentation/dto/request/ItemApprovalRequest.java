package com.eod.eod.domain.item.presentation.dto.request;

import com.eod.eod.domain.item.model.Item;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ItemApprovalRequest {

    @NotNull(message = "승인 결과는 필수입니다.")
    @Pattern(regexp = "^(APPROVED|REJECTED)$", message = "승인 결과는 APPROVED 또는 REJECTED만 가능합니다.")
    @JsonProperty("result")
    @Schema(description = "승인 처리 결과 (APPROVED 또는 REJECTED)", example = "APPROVED", requiredMode = Schema.RequiredMode.REQUIRED, allowableValues = {"APPROVED", "REJECTED"})
    private String result;

    public Item.ApprovalStatus toApprovalStatus() {
        return Item.ApprovalStatus.valueOf(result);
    }
}
