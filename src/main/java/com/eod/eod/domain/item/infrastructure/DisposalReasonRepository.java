package com.eod.eod.domain.item.infrastructure;

import com.eod.eod.domain.item.model.DisposalReason;
import com.eod.eod.domain.item.model.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface DisposalReasonRepository extends JpaRepository<DisposalReason, Long> {

    /**
     * 특정 아이템의 최신 폐기 보류 사유 조회 (Item ID로 조회)
     */
    Optional<DisposalReason> findTopByItemIdOrderByCreatedAtDesc(Long itemId);

    /**
     * 특정 아이템의 최신 폐기 보류 사유 조회 (Item 객체로 조회)
     */
    Optional<DisposalReason> findTopByItemOrderByCreatedAtDesc(Item item);

    /**
     * 특정 아이템의 특정 기간 내 최신 폐기 보류 사유 조회
     */
    Optional<DisposalReason> findTopByItemIdAndCreatedAtGreaterThanEqualAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long itemId, LocalDateTime fromDateTime, LocalDateTime toDateTimeExclusive);

    /**
     * 특정 아이템의 특정 기간 이후 최신 폐기 보류 사유 조회
     */
    Optional<DisposalReason> findTopByItemIdAndCreatedAtGreaterThanEqualOrderByCreatedAtDesc(
            Long itemId, LocalDateTime fromDateTime);

    /**
     * 특정 아이템의 특정 기간 이전 최신 폐기 보류 사유 조회
     */
    Optional<DisposalReason> findTopByItemIdAndCreatedAtLessThanOrderByCreatedAtDesc(
            Long itemId, LocalDateTime toDateTimeExclusive);

    /**
     * 특정 아이템의 폐기 보류 사유 존재 여부 확인
     */
    boolean existsByItem(Item item);
}
