package com.eod.eod.domain.item.application;

import com.eod.eod.common.annotation.RequireAdmin;
import com.eod.eod.domain.item.infrastructure.DisposalReasonRepository;
import com.eod.eod.domain.item.model.DisposalReason;
import com.eod.eod.domain.item.model.Item;
import com.eod.eod.domain.item.exception.InvalidParameterException;
import com.eod.eod.domain.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

@Service
@RequiredArgsConstructor
@Transactional
public class DisposalReasonService {

    private final ItemFacade itemFacade;
    private final DisposalReasonRepository disposalReasonRepository;

    /**
     * 폐기 보류 사유 제출
     */
    public DisposalReason submitDisposalReason(Long itemId, String reason, Integer extensionDays, User currentUser) {
        // 물품 조회
        Item item = itemFacade.getItemById(itemId);

        // 폐기 보류 사유 생성 (도메인에서 선생님 권한 및 물품 상태 검증)
        DisposalReason disposalReason = DisposalReason.create(item, currentUser, reason, extensionDays);

        // 저장 및 반환 (영속화된 엔티티 사용)
        return disposalReasonRepository.save(disposalReason);
    }

    /**
     * 폐기 보류 사유 조회 (날짜 필터 지원)
     */
    @Transactional(readOnly = true)
    public DisposalReason getDisposalReason(Long itemId, LocalDate from, LocalDate to) {
        // 물품 존재 여부 확인
        itemFacade.getItemById(itemId);

        // 날짜 필터가 있는 경우
        if (from != null || to != null) {
            if (from != null && to != null && from.isAfter(to)) {
                throw new InvalidParameterException("from은 to보다 이후일 수 없습니다.");
            }

            if (from != null && to != null) {
                LocalDateTime fromDateTime = from.atStartOfDay();
                LocalDateTime toDateTimeExclusive = to.plusDays(1).atStartOfDay();
                return disposalReasonRepository
                        .findTopByItemIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
                                itemId, fromDateTime, toDateTimeExclusive)
                        .orElseThrow(() -> new IllegalArgumentException("해당 기간에 폐기 보류 사유를 찾을 수 없습니다."));
            }
        }

        // 날짜 필터가 없으면 최신 사유 조회
        return disposalReasonRepository.findTopByItemIdOrderByCreatedAtDesc(itemId)
                .orElseThrow(() -> new IllegalArgumentException("해당 물품의 폐기 보류 사유를 찾을 수 없습니다."));
    }

    /**
     * 폐기 기간 연장
     */
    @RequireAdmin
    public String extendDisposalPeriod(Long itemId, Long reasonId, User currentUser) {
        // 물품 조회
        Item item = itemFacade.getItemById(itemId);

        // 보류 사유 조회 및 검증
        DisposalReason disposalReason = disposalReasonRepository.findById(reasonId)
                .orElseThrow(() -> new IllegalArgumentException("해당 보류 사유를 찾을 수 없습니다."));

        // 보류 사유가 해당 물품의 것인지 확인
        if (!disposalReason.getItem().equals(item)) {
            throw new IllegalArgumentException("해당 보류 사유는 이 물품의 것이 아닙니다.");
        }

        // 물품 상태 확인
        if (item.getStatus() != Item.ItemStatus.TO_BE_DISCARDED) {
            throw new IllegalStateException("폐기 예정 상태의 물품만 기간을 연장할 수 있습니다.");
        }

        // 폐기 기간 연장 (보류 사유에 저장된 일수만큼 연장)
        item.extendDisposalDate(disposalReason.getExtensionDays());

        return item.getDiscardedAt();
    }

    public long countItemsToBeDiscarded() {
        return itemFacade.countByStatus(Item.ItemStatus.TO_BE_DISCARDED);
    }
}
