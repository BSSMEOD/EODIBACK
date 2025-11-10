package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 장소 ID와 상태로 검색
    Page<Item> findByFoundPlaceIdAndStatus(Long foundPlaceId, Item.ItemStatus status, Pageable pageable);

    // 상태로만 검색
    Page<Item> findByStatus(Item.ItemStatus status, Pageable pageable);
}