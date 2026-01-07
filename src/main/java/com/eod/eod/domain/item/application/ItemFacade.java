package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.infrastructure.ItemRepository;
import com.eod.eod.domain.item.model.Item;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ItemFacade {

    private final ItemRepository itemRepository;

    // ID로 Item 조회 (존재하지 않으면 예외 발생)
    public Item getItemById(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 물품을 찾을 수 없습니다."));
    }

    public Item save(Item item) {
        return itemRepository.save(item);
    }

    public void delete(Item item) {
        itemRepository.delete(item);
    }

    public long countByStatus(Item.ItemStatus status) {
        return itemRepository.countByStatus(status);
    }

    public List<Item> findByStatusAndCreatedAtBefore(Item.ItemStatus status, LocalDateTime threshold) {
        return itemRepository.findByStatusAndCreatedAtBefore(status, threshold);
    }

    public List<Item> findByStatusAndDiscardedAtBefore(Item.ItemStatus status, LocalDateTime threshold) {
        return itemRepository.findByStatusAndDiscardedAtBefore(status, threshold);
    }
}
