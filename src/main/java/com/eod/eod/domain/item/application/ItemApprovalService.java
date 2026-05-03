package com.eod.eod.domain.item.application;

import com.eod.eod.common.annotation.RequireAdmin;
import com.eod.eod.common.event.EodBusinessEvent;
import org.springframework.context.ApplicationEventPublisher;
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
    private final ApplicationEventPublisher eventPublisher;

    // 물품 승인/거절 처리
    @RequireAdmin
    public ItemApprovalResponse processApproval(Long itemId, Item.ApprovalStatus approvalStatus, User currentUser) {
        String action = approvalStatus == Item.ApprovalStatus.APPROVED ? "approve" : "reject";
        // 물품 조회
        Item item = itemFacade.getItemById(itemId);

        // 승인/거절 처리
        item.processApproval(approvalStatus, currentUser);

        eventPublisher.publishEvent(new EodBusinessEvent("item", action, "success"));
        // Response 변환
        return ItemApprovalResponse.from(item);
    }
}
