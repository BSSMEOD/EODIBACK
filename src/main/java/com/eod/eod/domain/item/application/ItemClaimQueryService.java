package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.response.ClaimItemDto;
import com.eod.eod.domain.item.presentation.dto.response.ClaimItemListResponse;
import com.eod.eod.domain.item.presentation.dto.response.ClaimRequestsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemClaimQueryService {

    private final ItemClaimRepository itemClaimRepository;

    public long countPendingClaims() {
        return itemClaimRepository.countByStatus(ItemClaim.ClaimStatus.PENDING);
    }

    public ClaimItemListResponse getPendingClaimItems() {
        List<ClaimItemDto> items = itemClaimRepository.findItemsWithPendingClaims();

        return ClaimItemListResponse.of(items);
    }

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
        return ItemClaim.ClaimStatus.valueOf(status.toUpperCase());
    }
}
