package com.eod.eod.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EodMetrics {

    private final MeterRegistry meterRegistry;

    public void recordBusinessEvent(String domain, String action, String result) {
        Counter.builder("eod_business_events_total")
                .description("EOD domain business events")
                .tag("domain", domain)
                .tag("action", action)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
    }

    public void recordExternalCall(String provider, String operation, String result, Duration duration) {
        Timer.builder("eod_external_call_seconds")
                .description("External API call latency")
                .tag("provider", provider)
                .tag("operation", operation)
                .tag("result", result)
                .register(meterRegistry)
                .record(duration);
    }

    public void recordImageUpload(String result, long bytes, Duration duration) {
        recordBusinessEvent("image", "upload", result);
        DistributionSummary.builder("eod_image_upload_bytes")
                .description("Uploaded image size in bytes")
                .tag("result", result)
                .baseUnit("bytes")
                .register(meterRegistry)
                .record(bytes);
        Timer.builder("eod_image_upload_seconds")
                .description("Image upload latency")
                .tag("result", result)
                .register(meterRegistry)
                .record(duration);
    }

    public void recordSchedulerRun(String task, String result, int processedItems) {
        Counter.builder("eod_scheduler_runs_total")
                .description("Scheduled task executions")
                .tag("task", task)
                .tag("result", result)
                .register(meterRegistry)
                .increment();
        DistributionSummary.builder("eod_scheduler_processed_items")
                .description("Items processed by scheduled tasks")
                .tag("task", task)
                .register(meterRegistry)
                .record(processedItems);
    }
}
