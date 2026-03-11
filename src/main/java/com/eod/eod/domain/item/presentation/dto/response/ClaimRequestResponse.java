package com.eod.eod.domain.item.presentation.dto.response;

import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.user.model.User;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class ClaimRequestResponse {

    private Long requestId;
    private Long itemId;
    private String itemName;
    private String imageUrl;
    private String requesterName;
    private String requesterType;
    private LocalDateTime requestedAt;
    private String status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate visitDate;

    public static ClaimRequestResponse from(ItemClaim claim) {
        return ClaimRequestResponse.builder()
                .requestId(claim.getId())
                .itemId(claim.getItem().getId())
                .itemName(claim.getItem().getName())
                .imageUrl(claim.getItem().getImage())
                .requesterName(claim.getClaimant().getName())
                .requesterType(mapRoleToRequesterType(claim.getClaimant()))
                .requestedAt(claim.getClaimedAt())
                .status(claim.getStatus().name())
                .visitDate(claim.getVisitDate())
                .build();
    }

    private static String mapRoleToRequesterType(User user) {
        return switch (user.getRole()) {
            case ADMIN -> "ADMIN";
            case TEACHER -> "TEACHER";
            case USER -> "STUDENT";
        };
    }
}
