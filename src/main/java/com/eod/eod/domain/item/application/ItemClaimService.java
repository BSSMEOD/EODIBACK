package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemClaimRepository;
import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.model.ItemClaim;
import com.eod.eod.domain.item.presentation.dto.response.ItemClaimResponse;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemClaimService {

    private final ItemRepository itemRepository;
    private final ItemClaimRepository itemClaimRepository;

    public ItemClaimResponse claimItem(Long itemId, String claimReason, User currentUser) {
        // 아이템 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분실물을 찾을 수 없습니다."));

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
}
