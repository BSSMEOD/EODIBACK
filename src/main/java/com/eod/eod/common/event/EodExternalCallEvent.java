package com.eod.eod.common.event;

import java.time.Duration;

public record EodExternalCallEvent(String provider, String operation, String result, Duration duration) {
}
