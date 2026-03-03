package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long>, ItemRepositoryCustom {
    Optional<Item> findByIdAndDeletedAtIsNull(Long id);

    // 폐기 예정일이 지난 물품 조회 (자동 폐기용)
    List<Item> findByStatusAndDiscardedAtBeforeAndDeletedAtIsNull(Item.ItemStatus status, LocalDateTime dateTime);

    // 장기 방치된 분실물 조회 (자동 폐기 예정 전환용)
    List<Item> findByStatusAndCreatedAtBeforeAndDeletedAtIsNull(Item.ItemStatus status, LocalDateTime dateTime);

    // 특정 상태의 물품 개수 조회
    long countByStatusAndDeletedAtIsNull(Item.ItemStatus status);
}
