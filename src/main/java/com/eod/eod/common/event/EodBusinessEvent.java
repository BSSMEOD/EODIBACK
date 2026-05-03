package com.eod.eod.common.event;

public record EodBusinessEvent(String domain, String action, String result) {
}
