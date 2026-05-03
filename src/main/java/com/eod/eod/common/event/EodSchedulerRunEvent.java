package com.eod.eod.common.event;

public record EodSchedulerRunEvent(String task, String result, int processedItems) {
}
