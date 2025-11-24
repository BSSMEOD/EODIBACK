package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    // 장소 ID와 상태로 검색
    Page<Item> findByFoundPlaceIdAndStatus(Long foundPlaceId, Item.ItemStatus status, Pageable pageable);

    // 상태로만 검색
    Page<Item> findByStatus(Item.ItemStatus status, Pageable pageable);

    // 폐기 예정일이 지난 물품 조회 (자동 폐기용)
    List<Item> findByStatusAndDiscardedAtBefore(Item.ItemStatus status, LocalDateTime dateTime);

    // 장기 방치된 분실물 조회 (자동 폐기 예정 전환용)
    List<Item> findByStatusAndFoundAtBefore(Item.ItemStatus status, LocalDateTime dateTime);

    // 특정 상태의 물품 개수 조회
    long countByStatus(Item.ItemStatus status);
}