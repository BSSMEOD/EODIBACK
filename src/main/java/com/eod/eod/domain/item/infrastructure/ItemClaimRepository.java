package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.ItemClaim;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemClaimRepository extends JpaRepository<ItemClaim, Long>, ItemClaimRepositoryCustom {

    // 특정 사용자가 특정 아이템에 대해 이미 소유권 주장을 했는지 확인
    boolean existsByItemIdAndClaimantId(Long itemId, Long claimantId);

    // 특정 상태의 회수 신청 개수 조회
    long countByStatus(ItemClaim.ClaimStatus status);
}