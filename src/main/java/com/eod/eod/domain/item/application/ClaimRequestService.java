package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.response.ClaimRequestsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaimRequestService {

    private final ItemClaimRepository itemClaimRepository;

    public ClaimRequestsResponse getClaimRequests(Integer page, Integer size, String status) {
        ItemClaim.ClaimStatus claimStatus = parseStatus(status);

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "claimedAt")
        );

        Page<ItemClaim> claimPage = itemClaimRepository.findByStatus(claimStatus, pageable);

        return ClaimRequestsResponse.from(claimPage, page);
    }

    private ItemClaim.ClaimStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ItemClaim.ClaimStatus.PENDING;
        }

        try {
            return ItemClaim.ClaimStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ItemClaim.ClaimStatus.PENDING;
        }
    }
}