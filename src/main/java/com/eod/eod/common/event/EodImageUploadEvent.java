package com.eod.eod.common.event;

import java.time.Duration;

public record EodImageUploadEvent(String result, long bytes, Duration duration) {
}
