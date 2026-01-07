package com.eod.eod.common.util;

import com.eod.eod.domain.item.model.Item.DatePrecision;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public final class DatePrecisionParser {

    private static final DateTimeFormatter FULL_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private DatePrecisionParser() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static ParsedDate parse(String rawDate) {
        if (rawDate == null || rawDate.isBlank()) {
            throw new IllegalArgumentException("습득 날짜는 필수입니다.");
        }

        String trimmed = rawDate.trim();

        // yyyy 형식 (4자리)
        if (trimmed.matches("^\\d{4}$")) {
            return parseYear(trimmed);
        }

        // yyyy-MM 형식 (7자리)
        if (trimmed.matches("^\\d{4}-\\d{2}$")) {
            return parseYearMonth(trimmed);
        }

        // yyyy-MM-dd 형식 (10자리)
        if (trimmed.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            return parseFullDate(trimmed);
        }

        throw new IllegalArgumentException("올바른 날짜 형식이 아닙니다. (yyyy, yyyy-MM, yyyy-MM-dd)");
    }

    private static ParsedDate parseYear(String year) {
        try {
            int yearValue = Integer.parseInt(year);
            LocalDate date = LocalDate.of(yearValue, 1, 1);
            return new ParsedDate(date.atStartOfDay(), DatePrecision.YEAR);
        } catch (DateTimeParseException | NumberFormatException e) {
            throw new IllegalArgumentException("올바른 년도 형식이 아닙니다. (yyyy)", e);
        }
    }

    private static ParsedDate parseYearMonth(String yearMonth) {
        try {
            LocalDate date = LocalDate.parse(yearMonth + "-01", FULL_DATE_FORMATTER);
            return new ParsedDate(date.atStartOfDay(), DatePrecision.MONTH);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("올바른 년월 형식이 아닙니다. (yyyy-MM)", e);
        }
    }

    private static ParsedDate parseFullDate(String fullDate) {
        try {
            LocalDate date = LocalDate.parse(fullDate, FULL_DATE_FORMATTER);
            return new ParsedDate(date.atStartOfDay(), DatePrecision.DAY);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("올바른 날짜 형식이 아닙니다. (yyyy-MM-dd)", e);
        }
    }

    public static class ParsedDate {
        private final LocalDateTime dateTime;
        private final DatePrecision precision;

        public ParsedDate(LocalDateTime dateTime, DatePrecision precision) {
            this.dateTime = dateTime;
            this.precision = precision;
        }

        public LocalDateTime getDateTime() {
            return dateTime;
        }

        public DatePrecision getPrecision() {
            return precision;
        }
    }
}
