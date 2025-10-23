package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemApprovalService {

    private final ItemRepository itemRepository;

    // 물품 승인/거절 처리
    public Item processApproval(Long itemId, Item.ApprovalStatus approvalStatus, User currentUser) {
        // Admin 권한 검증 (User 도메인에서 예외 처리)
        currentUser.validateAdminRole();

        // 물품 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 분실물을 찾을 수 없습니다."));

        // 승인/거절 처리 (Item 도메인에서 처리)
        if (approvalStatus == Item.ApprovalStatus.APPROVED) {
            item.approve(currentUser);
        } else if (approvalStatus == Item.ApprovalStatus.REJECTED) {
            item.reject(currentUser);
        } else {
            throw new IllegalArgumentException("잘못된 승인 요청입니다.");
        }

        return item;
    }
}