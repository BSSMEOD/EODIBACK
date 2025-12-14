package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.model.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemSchedulerService {

    private final ItemFacade itemFacade;

    // 분실물 보관 기간 (6개월)
    private static final int RETENTION_MONTHS = 6;
    // 폐기 유예 기간 (2주)
    private static final int GRACE_PERIOD_WEEKS = 2;

    /**
     * 매일 자정(00:00)에 실행되어 장기 방치된 분실물을 폐기 예정 상태로 변경
     * 습득일로부터 (6개월 - 2주) 지난 분실물을 TO_BE_DISCARDED로 변경하고, 습득일 + 6개월을 폐기 예정일로 설정
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoMarkItemsAsToBeDiscarded() {
        log.info("폐기 예정 전환 스케줄러 시작");

        // (6개월 - 2주) 전 날짜 계산
        LocalDateTime thresholdDate = LocalDateTime.now()
                .minusMonths(RETENTION_MONTHS)
                .plusWeeks(GRACE_PERIOD_WEEKS);

        // LOST 상태이면서 습득일이 (6개월 - 2주) 이전인 물품 조회
        List<Item> longUnclaimedItems = itemFacade.findByStatusAndFoundAtBefore(
                Item.ItemStatus.LOST,
                thresholdDate
        );

        if (longUnclaimedItems.isEmpty()) {
            log.info("폐기 예정 전환 대상 물품이 없습니다.");
            return;
        }

        // 각 물품을 폐기 예정 상태로 변경
        int markedCount = 0;
        for (Item item : longUnclaimedItems) {
            try {
                item.markAsToBeDiscarded();
                markedCount++;
                log.info("물품 폐기 예정으로 전환 완료 - ID: {}, 이름: {}, 습득일: {}, 폐기 예정일: {}",
                        item.getId(), item.getName(), item.getFoundAt(), item.getDiscardedAt());
            } catch (Exception e) {
                log.error("물품 폐기 예정 전환 중 오류 발생 - ID: {}, 오류: {}", item.getId(), e.getMessage());
            }
        }

        log.info("폐기 예정 전환 스케줄러 완료 - 총 {}개 물품 전환 (습득일 + 6개월에 폐기 예정)", markedCount);
    }

    /**
     * 매일 자정(00:00)에 실행되어 폐기 예정일이 지난 물품을 자동으로 폐기 처리
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoDiscardExpiredItems() {
        log.info("자동 폐기 스케줄러 시작");

        LocalDateTime now = LocalDateTime.now();

        // 폐기 예정 상태이면서 폐기일이 지난 물품 조회
        List<Item> expiredItems = itemFacade.findByStatusAndDiscardedAtBefore(
                Item.ItemStatus.TO_BE_DISCARDED,
                now
        );

        if (expiredItems.isEmpty()) {
            log.info("폐기 대상 물품이 없습니다.");
            return;
        }

        // 각 물품을 폐기 처리
        int discardedCount = 0;
        for (Item item : expiredItems) {
            try {
                item.discard();
                discardedCount++;
                log.info("물품 자동 폐기 완료 - ID: {}, 이름: {}, 폐기 예정일: {}",
                        item.getId(), item.getName(), item.getDiscardedAt());
            } catch (Exception e) {
                log.error("물품 폐기 중 오류 발생 - ID: {}, 오류: {}", item.getId(), e.getMessage());
            }
        }

        log.info("자동 폐기 스케줄러 완료 - 총 {}개 물품 폐기", discardedCount);
    }
}
