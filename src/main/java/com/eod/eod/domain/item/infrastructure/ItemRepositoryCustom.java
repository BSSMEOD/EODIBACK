package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ItemRepositoryCustom {

    /**
     * 동적 쿼리를 사용한 물품 검색
     * @param placeIds 장소 ID 리스트 (선택 사항)
     * @param status 물품 상태 (선택 사항)
     * @param foundAtFrom 습득일 시작 날짜 (선택 사항)
     * @param foundAtTo 습득일 종료 날짜 (선택 사항)
     * @param category 물품 카테고리 (선택 사항)
     * @param pageable 페이징 정보
     * @return 검색된 물품 페이지
     */
    Page<Item> searchItems(String trimmedQuery,List<Long> placeIds, Item.ItemStatus status,
                          LocalDate foundAtFrom, LocalDate foundAtTo, 
                          Item.ItemCategory category, Pageable pageable);
}
