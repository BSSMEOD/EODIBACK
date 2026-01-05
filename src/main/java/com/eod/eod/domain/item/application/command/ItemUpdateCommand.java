package com.eod.eod.domain.item.application.command;

import com.eod.eod.domain.item.model.Item;

/**
 * 아이템 수정 커맨드
 *
 * 아이템 수정에 필요한 모든 정보를 캡슐화합니다.
 */
public record ItemUpdateCommand(
        String name,
        Integer reporterStudentCode,
        String reporterName,
        String foundAt,
        Long placeId,
        String foundPlaceDetail,
        String imageUrl,
        Item.ItemCategory category
) {
}
