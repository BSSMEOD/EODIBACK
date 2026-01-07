package com.eod.eod.domain.item.application;

import com.eod.eod.common.annotation.RequireAdmin;
import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.response.ItemClaimResponse;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemClaimService {

    private final ItemFacade itemFacade;
    private final ItemClaimRepository itemClaimRepository;

    public ItemClaimResponse claimItem(Long itemId, String claimReason, User currentUser) {
        // 아이템 존재 여부 확인
        Item item = itemFacade.getItemById(itemId);

        // 중복 주장 확인
        if (itemClaimRepository.existsByItemIdAndClaimantId(itemId, currentUser.getId())) {
            throw new IllegalStateException("이미 해당 분실물에 대해 소유권을 주장하셨습니다.");
        }

        // 소유권 주장 생성
        ItemClaim claim = ItemClaim.builder()
                .item(item)
                .claimant(currentUser)
                .claimReason(claimReason)
                .build();

        itemClaimRepository.save(claim);

        return ItemClaimResponse.success();
    }

    /**
     * 소유권 주장 승인
     * 같은 물품에 대한 다른 PENDING 상태의 주장들은 자동으로 거절됨
     */
    @RequireAdmin
    public void approveClaim(Long claimId, User currentUser) {
        // 소유권 주장 조회
        ItemClaim claim = itemClaimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("해당 소유권 주장을 찾을 수 없습니다."));

        // 승인 처리
        claim.approve();

        // 같은 물품에 대한 다른 PENDING 상태의 주장들을 모두 거절
        Long itemId = claim.getItem().getId();
        List<ItemClaim> otherPendingClaims = itemClaimRepository
                .findByItemIdAndStatus(itemId, ItemClaim.ClaimStatus.PENDING);

        for (ItemClaim otherClaim : otherPendingClaims) {
            if (!otherClaim.getId().equals(claimId)) {
                otherClaim.reject();
            }
        }
    }


}
