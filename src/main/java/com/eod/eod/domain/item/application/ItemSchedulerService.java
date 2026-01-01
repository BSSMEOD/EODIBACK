package com.eod.eod.domain.item.application;

import com.eod.eod.domain.item.model.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
        LocalDateTime thresholdDate = LocalDateTime.now()
                .minusMonths(RETENTION_MONTHS)
                .plusWeeks(GRACE_PERIOD_WEEKS);

        processScheduledTask(
                "폐기 예정 전환",
                () -> itemFacade.findByStatusAndFoundAtBefore(Item.ItemStatus.LOST, thresholdDate),
                Item::markAsToBeDiscarded,
                item -> String.format("ID: %d, 이름: %s, 습득일: %s, 폐기 예정일: %s",
                        item.getId(), item.getName(), item.getFoundAt(), item.getDiscardedAt())
        );
    }

    /**
     * 매일 자정(00:00)에 실행되어 폐기 예정일이 지난 물품을 자동으로 폐기 처리
     */
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void autoDiscardExpiredItems() {
        LocalDateTime now = LocalDateTime.now();

        processScheduledTask(
                "자동 폐기",
                () -> itemFacade.findByStatusAndDiscardedAtBefore(Item.ItemStatus.TO_BE_DISCARDED, now),
                Item::discard,
                item -> String.format("ID: %d, 이름: %s, 폐기 예정일: %s",
                        item.getId(), item.getName(), item.getDiscardedAt())
        );
    }

    /**
     * 스케줄 작업 공통 처리 로직
     */
    private void processScheduledTask(
            String taskName,
            Supplier<List<Item>> itemsFetcher,
            Consumer<Item> itemProcessor,
            Function<Item, String> itemInfoProvider
    ) {
        log.info("{} 스케줄러 시작", taskName);

        List<Item> items = itemsFetcher.get();

        if (items.isEmpty()) {
            log.info("{} 대상 물품이 없습니다.", taskName);
            return;
        }

        int processedCount = 0;
        for (Item item : items) {
            try {
                itemProcessor.accept(item);
                processedCount++;
                log.info("{} 완료 - {}", taskName, itemInfoProvider.apply(item));
            } catch (Exception e) {
                log.error("{} 중 오류 발생 - ID: {}, 오류: {}", taskName, item.getId(), e.getMessage());
            }
        }

        log.info("{} 스케줄러 완료 - 총 {}개 물품 처리", taskName, processedCount);
    }
}
