package com.eod.eod.common.util;

import com.eod.eod.domain.item.model.Item.DatePrecision;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class DatePrecisionFormatter {

    private static final DateTimeFormatter YEAR_FORMATTER = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DatePrecisionFormatter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static String format(LocalDateTime dateTime, DatePrecision precision) {
        if (dateTime == null) {
            return null;
        }

        if (precision == null) {
            precision = DatePrecision.DAY; // 기본값
        }

        return switch (precision) {
            case YEAR -> dateTime.format(YEAR_FORMATTER);
            case MONTH -> dateTime.format(YEAR_MONTH_FORMATTER);
            case DAY -> dateTime.format(FULL_DATE_FORMATTER);
        };
    }
}
