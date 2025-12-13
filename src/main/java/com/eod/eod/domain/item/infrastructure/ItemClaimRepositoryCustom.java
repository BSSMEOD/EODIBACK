package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.presentation.dto.response.ClaimItemDto;

import java.util.List;

public interface ItemClaimRepositoryCustom {

    /**
     * 회수 신청이 있는 분실물 목록 조회
     * PENDING 상태의 회수 신청이 1건 이상 있는 분실물만 조회
     * DISCARDED, GIVEN 상태의 분실물은 제외
     */
    List<ClaimItemDto> findItemsWithPendingClaims();
}
