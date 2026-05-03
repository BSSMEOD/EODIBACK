package com.eod.eod.common.metrics;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EodMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private EodMetrics eodMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        eodMetrics = new EodMetrics(meterRegistry);
    }

    @Test
    @DisplayName("비즈니스 이벤트를 domain/action/result 라벨로 기록한다")
    void recordBusinessEvent() {
        eodMetrics.recordBusinessEvent("item", "register", "success");

        double count = meterRegistry.get("eod_business_events_total")
                .tag("domain", "item")
                .tag("action", "register")
                .tag("result", "success")
                .counter()
                .count();

        assertThat(count).isEqualTo(1.0);
    }

    @Test
    @DisplayName("외부 API 호출 지연시간을 provider/operation/result 라벨로 기록한다")
    void recordExternalCall() {
        eodMetrics.recordExternalCall("bsm", "token", "success", Duration.ofMillis(120));

        long count = meterRegistry.get("eod_external_call_seconds")
                .tag("provider", "bsm")
                .tag("operation", "token")
                .tag("result", "success")
                .timer()
                .count();

        assertThat(count).isEqualTo(1);
    }

    @Test
    @DisplayName("스케줄러 실행 결과와 처리 건수를 기록한다")
    void recordSchedulerRun() {
        eodMetrics.recordSchedulerRun("auto_discard_expired_items", "success", 3);

        double eventCount = meterRegistry.get("eod_scheduler_runs_total")
                .tag("task", "auto_discard_expired_items")
                .tag("result", "success")
                .counter()
                .count();
        double processedCount = meterRegistry.get("eod_scheduler_processed_items")
                .tag("task", "auto_discard_expired_items")
                .summary()
                .totalAmount();

        assertThat(eventCount).isEqualTo(1.0);
        assertThat(processedCount).isEqualTo(3.0);
    }
}
