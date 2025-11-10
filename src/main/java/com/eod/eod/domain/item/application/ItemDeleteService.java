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
public class ItemDeleteService {

    private final ItemRepository itemRepository;

    public void deleteItem(Long itemId, User currentUser) {
        // ADMIN 권한 확인
        if (!currentUser.isAdmin()) {
            throw new IllegalStateException("ADMIN 권한이 필요합니다.");
        }

        // 물품 존재 여부 확인
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 물품을 찾을 수 없습니다."));

        // 물품 삭제
        itemRepository.delete(item);
    }
}
