package com.eod.eod.common.metrics;

import com.eod.eod.common.event.EodBusinessEvent;
import com.eod.eod.common.event.EodExternalCallEvent;
import com.eod.eod.common.event.EodImageUploadEvent;
import com.eod.eod.common.event.EodSchedulerRunEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EodMetricsListener {

    private final EodMetrics eodMetrics;

    @EventListener
    public void onBusinessEvent(EodBusinessEvent event) {
        eodMetrics.recordBusinessEvent(event.domain(), event.action(), event.result());
    }

    @EventListener
    public void onExternalCall(EodExternalCallEvent event) {
        eodMetrics.recordExternalCall(event.provider(), event.operation(), event.result(), event.duration());
    }

    @EventListener
    public void onImageUpload(EodImageUploadEvent event) {
        eodMetrics.recordImageUpload(event.result(), event.bytes(), event.duration());
    }

    @EventListener
    public void onSchedulerRun(EodSchedulerRunEvent event) {
        eodMetrics.recordSchedulerRun(event.task(), event.result(), event.processedItems());
    }
}
