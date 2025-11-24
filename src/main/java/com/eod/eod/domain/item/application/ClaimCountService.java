package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.ItemClaim;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClaimCountService {

    private final ItemClaimRepository itemClaimRepository;

    public long getClaimCount() {
        return itemClaimRepository.countByStatus(ItemClaim.ClaimStatus.PENDING);
    }
}
