package com.eod.eod.domain.item.presentation.dto.response;

import com.eod.eod.domain.item.model.ItemClaim;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;

@Getter
@Builder
public class MyClaimResponse {

    private Long claimId;
    private Long itemId;
    private String itemName;
    private String imageUrl;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate claimedAt;

    private String status;

    public static MyClaimResponse from(ItemClaim claim) {
        return MyClaimResponse.builder()
                .claimId(claim.getId())
                .itemId(claim.getItem().getId())
                .itemName(claim.getItem().getName())
                .imageUrl(claim.getItem().getImage())
                .claimedAt(claim.getClaimedAt().toLocalDate())
                .status(claim.getStatus().name())
                .build();
    }
}
