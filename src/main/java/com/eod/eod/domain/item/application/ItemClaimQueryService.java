package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.response.ClaimItemResponse;
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
        List<ClaimItemResponse> items = itemClaimRepository.findItemsWithPendingClaims();

        return ClaimItemListResponse.of(items);
    }

    public ClaimRequestsResponse getClaimRequests(Integer page, Integer size, String status, String sort) {
        Sort sortBy = parseSort(sort);

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                sortBy
        );

        Page<ItemClaim> claimPage;

        // status가 null이거나 비어있으면 전체 조회
        if (status == null || status.isBlank()) {
            claimPage = itemClaimRepository.findAll(pageable);
        } else {
            ItemClaim.ClaimStatus claimStatus = ItemClaim.ClaimStatus.valueOf(status.toUpperCase());
            claimPage = itemClaimRepository.findByStatus(claimStatus, pageable);
        }

        return ClaimRequestsResponse.from(claimPage, page);
    }

    private ItemClaim.ClaimStatus parseStatus(String status) {
        if (status == null || status.isBlank()) {
            return ItemClaim.ClaimStatus.PENDING;
        }
        return ItemClaim.ClaimStatus.valueOf(status.toUpperCase());
    }

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "claimedAt"); // 기본값: 최신순
        }

        String upperSort = sort.trim().toUpperCase();
        return switch (upperSort) {
            case "LATEST" -> Sort.by(Sort.Direction.DESC, "claimedAt");
            case "OLDEST" -> Sort.by(Sort.Direction.ASC, "claimedAt");
            default -> throw new IllegalArgumentException("유효하지 않은 정렬 방식입니다: " + sort);
        };
    }
}
