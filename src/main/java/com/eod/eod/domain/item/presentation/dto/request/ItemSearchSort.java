package com.eod.eod.domain.item.presentation.dto.request;

import lombok.Getter;
import org.springframework.data.domain.Sort;

/**
 * 물품 검색 정렬 옵션
 * API 요청에서 사용되는 정렬 방식을 정의합니다.
 */
@Getter
public enum ItemSearchSort {
    /**
     * 최신순 (습득일 기준 내림차순)
     */
    LATEST("foundAt", Sort.Direction.DESC),
    
    /**
     * 과거순 (습득일 기준 오름차순)
     */
    OLDEST("foundAt", Sort.Direction.ASC);

    private final String field;
    private final Sort.Direction direction;

    ItemSearchSort(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }

    /**
     * Spring Data의 Sort 객체로 변환
     * 
     * @return Sort 객체
     */
    public Sort toSort() {
        return Sort.by(direction, field);
    }
}
