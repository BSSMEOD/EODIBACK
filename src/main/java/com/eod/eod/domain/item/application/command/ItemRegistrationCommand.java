package com.eod.eod.domain.item.application.command;

import com.eod.eod.domain.item.model.Item;

/**
 * 아이템 등록 커맨드
 *
 * 아이템 등록에 필요한 모든 정보를 캡슐화합니다.
 */
public record ItemRegistrationCommand(
        String name,
        Integer reporterStudentCode,
        String reporterName,
        String foundAt,
        Long placeId,
        String placeDetail,
        String imageUrl,
        Item.ItemCategory category
) {
}
