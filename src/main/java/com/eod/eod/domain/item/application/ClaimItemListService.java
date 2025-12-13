package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.presentation.dto.response.ClaimItemDto;
import com.eod.eod.domain.item.presentation.dto.response.ClaimItemListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaimItemListService {

    private final ItemClaimRepository itemClaimRepository;

    public ClaimItemListResponse getItemsWithClaims() {
        List<ClaimItemDto> items = itemClaimRepository.findItemsWithPendingClaims();

        if (items.isEmpty()) {
            throw new IllegalStateException("회수 신청 요청이 존재하지 않습니다.");
        }

        return ClaimItemListResponse.of(items);
    }
}
