package com.eod.eod.common.metrics;

import static org.mockito.Mockito.verify;

import com.eod.eod.common.event.EodBusinessEvent;
import com.eod.eod.common.event.EodExternalCallEvent;
import com.eod.eod.common.event.EodImageUploadEvent;
import com.eod.eod.common.event.EodSchedulerRunEvent;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EodMetricsListenerTest {

    @Mock
    private EodMetrics eodMetrics;

    @InjectMocks
    private EodMetricsListener listener;

    @Test
    @DisplayName("비즈니스 이벤트를 메트릭 기록으로 변환한다")
    void recordsBusinessEvent() {
        listener.onBusinessEvent(new EodBusinessEvent("item", "register", "success"));

        verify(eodMetrics).recordBusinessEvent("item", "register", "success");
    }

    @Test
    @DisplayName("외부 API 이벤트를 메트릭 기록으로 변환한다")
    void recordsExternalCallEvent() {
        Duration duration = Duration.ofMillis(120);

        listener.onExternalCall(new EodExternalCallEvent("bsm", "token", "success", duration));

        verify(eodMetrics).recordExternalCall("bsm", "token", "success", duration);
    }

    @Test
    @DisplayName("이미지 업로드 이벤트를 메트릭 기록으로 변환한다")
    void recordsImageUploadEvent() {
        Duration duration = Duration.ofMillis(250);

        listener.onImageUpload(new EodImageUploadEvent("success", 1024L, duration));

        verify(eodMetrics).recordImageUpload("success", 1024L, duration);
    }

    @Test
    @DisplayName("스케줄러 실행 이벤트를 메트릭 기록으로 변환한다")
    void recordsSchedulerRunEvent() {
        listener.onSchedulerRun(new EodSchedulerRunEvent("auto_discard_expired_items", "success", 3));

        verify(eodMetrics).recordSchedulerRun("auto_discard_expired_items", "success", 3);
    }
}
