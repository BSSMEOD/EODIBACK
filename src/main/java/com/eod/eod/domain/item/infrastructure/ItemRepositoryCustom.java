package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryCustom {

    /**
     * 동적 쿼리를 사용한 물품 검색
     * @param placeId 장소 ID (선택 사항)
     * @param status 물품 상태 (선택 사항)
     * @param pageable 페이징 정보
     * @return 검색된 물품 페이지
     */
    Page<Item> searchItems(Long placeId, Item.ItemStatus status, Pageable pageable);
}
