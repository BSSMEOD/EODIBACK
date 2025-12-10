package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.presentation.dto.response.ItemApprovalResponse;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemApprovalService {

    private final ItemFacade itemFacade;

    // 물품 승인/거절 처리
    public ItemApprovalResponse processApproval(Long itemId, Item.ApprovalStatus approvalStatus, User currentUser) {
        // 물품 조회
        Item item = itemFacade.getItemById(itemId);

        // 승인/거절 처리 (Item 도메인에서 모든 검증 및 상태 변경)
        item.processApproval(approvalStatus, currentUser);

        // Response 변환
        return ItemApprovalResponse.from(item);
    }
}
