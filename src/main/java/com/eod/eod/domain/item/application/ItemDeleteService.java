package com.eod.eod.domain.item.application;

import com.eod.eod.common.annotation.RequireAdmin;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemDeleteService {

    private final ItemFacade itemFacade;

    @RequireAdmin
    public void deleteItem(Long itemId, User currentUser) {
        // 물품 존재 여부 확인
        Item item = itemFacade.getItemById(itemId);

        // 물품 삭제
        itemFacade.delete(item);
    }
}
