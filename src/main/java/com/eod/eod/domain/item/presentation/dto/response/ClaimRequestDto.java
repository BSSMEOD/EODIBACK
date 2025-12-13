package com.eod.eod.domain.item.presentation.dto.response;

import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.user.model.User;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ClaimRequestDto {

    private Long requestId;
    private Long itemId;
    private String itemName;
    private String thumbnailUrl;
    private String requestMessage;
    private String requesterName;
    private String requesterType;
    private LocalDateTime requestedAt;
    private String status;

    public static ClaimRequestDto from(ItemClaim claim) {
        return ClaimRequestDto.builder()
                .requestId(claim.getId())
                .itemId(claim.getItem().getId())
                .itemName(claim.getItem().getName())
                .thumbnailUrl(claim.getItem().getImage())
                .requestMessage(claim.getClaimReason())
                .requesterName(claim.getClaimant().getName())
                .requesterType(mapRoleToRequesterType(claim.getClaimant()))
                .requestedAt(claim.getClaimedAt())
                .status(claim.getStatus().name())
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
